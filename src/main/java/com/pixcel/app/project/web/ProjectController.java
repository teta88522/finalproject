package com.pixcel.app.project.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.IssueStatVO;
import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectModulesVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;

@Controller
public class ProjectController {

	@Autowired
	private ProjectService projectService;

	/* 프로젝트 등록 폼 */
	@GetMapping("/myproject/register")
	public String registerForm(Model model, @CookieValue(value = "userId", required = false) String userId) {
		model.addAttribute("userId", userId);
		return "project/project";
	}

	/* 프로젝트 등록 처리 */
	@PostMapping("/myproject/register")
	public String registerProject(ProjectVO projectVO, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@CookieValue(value = "userId", required = false) String userId, RedirectAttributes redirectAttributes) {
		if (projectVO.getOwnerId() == null || projectVO.getOwnerId().isEmpty()) {
			projectVO.setOwnerId(userId);
		}

		// ✅ 추가: 날짜 검증 (startDate > endDate 불가) - Repository 방식 동일
		if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date startDateObj = dateFormat.parse(startDate);
				Date endDateObj = dateFormat.parse(endDate);

				if (startDateObj.after(endDateObj)) {
					redirectAttributes.addFlashAttribute("errorMessage", "시작일이 완료일보다 늦을 수 없습니다.");
					return "redirect:/myproject/register";
				}

				// ✅ projectVO에 Date 객체 설정 (DB 저장용)
				projectVO.setStartDate(startDateObj);
				projectVO.setEndDate(endDateObj);

			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("errorMessage", "날짜 형식이 올바르지 않습니다.");
				return "redirect:/myproject/register";
			}
		}

		projectService.registerProject(projectVO);
		return "redirect:/myproject/list";
	}

	/* 프로젝트 목록 (검색 + 페이징) */
	@GetMapping("/myproject/list")
	public String projectListForm(Model model, @CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			@RequestParam(value = "searchProjectName", required = false) String searchProjectName,
			@RequestParam(value = "searchOwnerName", required = false) String searchOwnerName,
			@RequestParam(value = "searchStatusCode", required = false) String searchStatusCode,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

		if (subscribeYn == null || subscribeYn.isEmpty()) {
			subscribeYn = "N";
		}

		ProjectVO searchVO = new ProjectVO();
		searchVO.setOwnerId(userId);
		searchVO.setSearchProjectName(searchProjectName);
		searchVO.setSearchOwnerName(searchOwnerName);
		searchVO.setSearchStatusCode(searchStatusCode);
		searchVO.setPage(page);
		searchVO.setPageSize(pageSize);

		List<ProjectVO> projectList;
		if ("Y".equals(subscribeYn)) {
			projectList = projectService.selectMyCreatedProjectList(searchVO);
		} else {
			projectList = projectService.selectMyJoinedProjectList(searchVO);
		}

		model.addAttribute("projectList", projectList);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("searchVO", searchVO);

		return "project/projectList";
	}

	/* ✅ 추가: 관리자용 전체 프로젝트 조회 (/manage/projects) */
	@GetMapping("/pm/list")
	public String manageProjects(Model model,
			@RequestParam(value = "searchProjectName", required = false) String searchProjectName,
			@RequestParam(value = "searchStatusCode", required = false) String searchStatusCode,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

		ProjectVO searchVO = new ProjectVO();
		searchVO.setSearchProjectName(searchProjectName);
		searchVO.setSearchStatusCode(searchStatusCode);
		searchVO.setPage(page);
		searchVO.setPageSize(pageSize);

		List<ProjectVO> projectList = projectService.selectAllProjectsWithSearch(searchVO);

		model.addAttribute("projectList", projectList);
		model.addAttribute("searchVO", searchVO);
		model.addAttribute("searchProjectName", searchProjectName);
		model.addAttribute("searchStatusCode", searchStatusCode);

		return "project/pmProjectList";
	}

	/* 프로젝트 상세 */
	@GetMapping("/projectdetail/{projectId}")
	public String projectDetail(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		// ✅ 1. 로그인 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		// ✅ 2. 프로젝트 존재 확인
		ProjectVO project = projectService.selectProjectDetail(projectId);

		if (project == null) {
			return "redirect:/myproject/list";
		}

		// ✅ 3. 접근 권한 확인 (소유자 또는 구성원)
		// ⚡ 소유자인 경우 구성원 여부 조회(countProjectMember)를 생략해 왕복 1회 절약
		boolean isOwner = project.getOwnerId().equals(userId);
		boolean isMember = isOwner || projectService.isProjectMember(projectId, userId);

		if (!isOwner && !isMember) {
			return "redirect:/myproject/list";
		}

		// ✅ 4. 정상 접근 - 데이터 조회
		// ⚡ 서로 결과가 필요 없는 독립적인 조회이므로 동시에 실행 (순차 대비 왕복 1회분 절약)
		java.util.concurrent.CompletableFuture<List<ProjectMemberVO>> memberListFuture =
				java.util.concurrent.CompletableFuture
						.supplyAsync(() -> projectService.selectProjectMemberList(projectId));

		java.util.concurrent.CompletableFuture<List<IssueStatVO>> issueStatFuture =
				java.util.concurrent.CompletableFuture
						.supplyAsync(() -> projectService.selectIssueStatByProjectId(projectId));

		List<ProjectMemberVO> projectMemberList = memberListFuture.join();
		List<IssueStatVO> issueStatList = issueStatFuture.join();

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectMemberList", projectMemberList);
		model.addAttribute("issueStatList", issueStatList);

		return "project/projectDetail";
	}

	/* 구성원 목록 설정 */
	@GetMapping("/project/{projectId}/settings/members")
	public String projectMemberSetting(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		List<ProjectMemberVO> projectMemberList = projectService.selectProjectMemberList(projectId);

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectMemberList", projectMemberList);

		return "settings/projectMemberSetting";
	}

	/* 구성원 추가 폼 */
	@GetMapping("/project/{projectId}/settings/members/new")
	public String projectMemberAddForm(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		List<ProjectMemberVO> candidateList = projectService.selectProjectMemberCandidateList(projectId);
		List<ProjectRoleVO> projectRoleList = projectService.selectProjectRoleList(projectId);

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("candidateList", candidateList);
		model.addAttribute("projectRoleList", projectRoleList);
		model.addAttribute("projectMemberVO", new ProjectMemberVO());

		return "settings/projectMemberAdd";
	}

	/* 구성원 추가 처리 */
	@PostMapping("/project/{projectId}/settings/members/add")
	public String insertProjectMember(@PathVariable String projectId, ProjectMemberVO projectMemberVO,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		projectMemberVO.setProjectId(projectId);
		Map<String, Object> resultMap = projectService.insertProjectMember(projectMemberVO);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 구성원 역할 수정 폼 */
	@GetMapping("/project/{projectId}/settings/members/{projectMemberId}/update")
	public String projectMemberUpdateForm(@PathVariable String projectId, @PathVariable String projectMemberId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		ProjectMemberVO projectMember = projectService.selectProjectMemberDetail(projectMemberId);
		List<ProjectRoleVO> projectRoleList = projectService.selectProjectRoleList(projectId);

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectMember", projectMember);
		model.addAttribute("projectRoleList", projectRoleList);

		return "settings/projectMemberUpdate";
	}

	/* 구성원 역할 수정 처리 */
	@PostMapping("/project/{projectId}/settings/members/update")
	public String updateProjectMemberRole(@PathVariable String projectId, ProjectMemberVO projectMemberVO,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		projectMemberVO.setProjectId(projectId);
		Map<String, Object> resultMap = projectService.updateProjectMemberRole(projectMemberVO);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 구성원 삭제 */
	@PostMapping("/project/{projectId}/settings/members/delete")
	public String deleteProjectMember(@PathVariable String projectId, @RequestParam String projectMemberId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		Map<String, Object> resultMap = projectService.deleteProjectMember(projectMemberId);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 프로젝트 모듈 설정 */
	@GetMapping("/project/{projectId}/settings/modules")
	public String projectModuleSetting(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		List<ProjectModulesVO> projectModuleList = projectService.selectAllModuleProjects(projectId);

		// ✅ 전체 모듈 코드-이름 매핑 (common_code 테이블 CODE_ID2='프로젝트 모듈' 기준과 반드시 동일해야 함)
		java.util.LinkedHashMap<String, String> moduleMap = new java.util.LinkedHashMap<>();
		moduleMap.put("h001", "일감관리");
		moduleMap.put("h003", "파일");
		moduleMap.put("h004", "저장소");
		moduleMap.put("h005", "문서");
		moduleMap.put("h006", "위키");
		moduleMap.put("h007", "게시판");
		moduleMap.put("h008", "간트차트");
		moduleMap.put("h009", "칸반");
		moduleMap.put("h010", "로드맵");
		moduleMap.put("h011", "마일스톤");
		moduleMap.put("h012", "달력");

		java.util.Set<String> usedModuleCodes = new java.util.HashSet<>();
		for (ProjectModulesVO pm : projectModuleList) {
			usedModuleCodes.add(pm.getModuleCode());
		}

		// ✅ 아직 추가하지 않은(선택 가능한) 모듈 목록 (뷰에서의 복잡한 연산 방지)
		java.util.LinkedHashMap<String, String> availableModules = new java.util.LinkedHashMap<>();
		for (java.util.Map.Entry<String, String> entry : moduleMap.entrySet()) {
			if (!usedModuleCodes.contains(entry.getKey())) {
				availableModules.put(entry.getKey(), entry.getValue());
			}
		}

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectModuleList", projectModuleList);
		model.addAttribute("moduleMap", moduleMap);
		model.addAttribute("usedModuleCodes", usedModuleCodes);
		model.addAttribute("availableModules", availableModules);

		return "settings/projectModuleSetting";
	}

	/* ✅ 요청하신 경로 별칭: /myproject/module?projectId=xxx 로도 같은 화면에 접근 가능 */
	@GetMapping("/myproject/update")
	public String projectModuleSettingAlias(@RequestParam String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		return projectModuleSetting(projectId, userId, subscribeYn, model);
	}

	/* 프로젝트 모듈 추가 (✅ 체크박스로 여러 개 동시 추가 가능하도록 List로 변경) */
	@PostMapping("/project/{projectId}/settings/modules/add")
	public String insertProjectModule(@PathVariable String projectId,
			@RequestParam(required = false) java.util.List<String> moduleCode,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		if (moduleCode == null || moduleCode.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "추가할 모듈을 하나 이상 선택해주세요.");
			return "redirect:/project/" + projectId + "/settings/modules";
		}

		int successCount = 0;
		for (String code : moduleCode) {
			Map<String, Object> resultMap = projectService.insertProjectModule(projectId, code);
			if (Boolean.TRUE.equals(resultMap.get("success")) || resultMap.get("success") == null) {
				successCount++;
			}
		}
//		com.pixcel.app.web.GlobalControllerAdvice.evictModuleCache(projectId); // ✅ 사이드바 캐시 무효화
		redirectAttributes.addFlashAttribute("message", successCount + "개 모듈이 추가되었습니다.");

		return "redirect:/project/" + projectId + "/settings/modules";
	}

	/* 프로젝트 모듈 삭제 (✅ 체크박스로 여러 개 동시 삭제 가능하도록 List로 변경) */
	@PostMapping("/project/{projectId}/settings/modules/delete")
	public String deleteProjectModule(@PathVariable String projectId,
			@RequestParam(required = false) java.util.List<String> moduleCode,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		if (moduleCode == null || moduleCode.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "제거할 모듈을 하나 이상 선택해주세요.");
			return "redirect:/project/" + projectId + "/settings/modules";
		}

		int successCount = 0;
		for (String code : moduleCode) {
			Map<String, Object> resultMap = projectService.deleteProjectModule(projectId, code);
			if (Boolean.TRUE.equals(resultMap.get("success")) || resultMap.get("success") == null) {
				successCount++;
			}
		}
//		com.pixcel.app.web.GlobalControllerAdvice.evictModuleCache(projectId); // ✅ 사이드바 캐시 무효화
		redirectAttributes.addFlashAttribute("message", successCount + "개 모듈이 제거되었습니다.");

		return "redirect:/project/" + projectId + "/settings/modules";
	}

	/* 프로젝트 삭제 */
	@PostMapping("/myproject/delete")
	public String deleteProject(@RequestParam String projectId,
			@CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {

		// ✅ 권한 확인: 로그인 + 소유자 확인
		if (userId == null || userId.isEmpty()) {
			return "redirect:/login";
		}

		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || !project.getOwnerId().equals(userId)) {
			return "redirect:/myproject/list";
		}

		if (!"Y".equals(subscribeYn))
			return "redirect:/myproject/list";

		Map<String, Object> resultMap = projectService.deleteProject(projectId);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/myproject/list";
	}

	/* 관리자용 프로젝트 삭제 (JSON 응답) */
	@PostMapping("/pm/{projectId}/delete")
	@ResponseBody
	public Map<String, Object> deleteProjectFromAdmin(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId) {

		Map<String, Object> result = new HashMap<>();

		// 로그인 확인
		if (userId == null || userId.isEmpty()) {
			result.put("success", false);
			result.put("message", "로그인이 필요합니다.");
			return result;
		}

		// 프로젝트 조회
		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null) {
			result.put("success", false);
			result.put("message", "프로젝트가 없습니다.");
			return result;
		}

		// 소유자 확인
		if (!project.getOwnerId().equals(userId)) {
			result.put("success", false);
			result.put("message", "삭제 권한이 없습니다.");
			return result;
		}

		// 삭제 실행
		Map<String, Object> deleteResult = projectService.deleteProject(projectId);
		return deleteResult;
	}

	/* 관리자용 프로젝트 잠금 (JSON 응답) */
	@PostMapping("/pm/{projectId}/lock")
	@ResponseBody
	public Map<String, Object> lockProjectFromAdmin(@PathVariable String projectId,
			@CookieValue(value = "userId", required = false) String userId) {

		Map<String, Object> result = new HashMap<>();

		// 로그인 확인
		if (userId == null || userId.isEmpty()) {
			result.put("success", false);
			result.put("message", "로그인이 필요합니다.");
			return result;
		}

		// 프로젝트 조회
		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null) {
			result.put("success", false);
			result.put("message", "프로젝트가 없습니다.");
			return result;
		}

		// 소유자 확인
		if (!project.getOwnerId().equals(userId)) {
			result.put("success", false);
			result.put("message", "권한이 없습니다.");
			return result;
		}

		// 잠금 상태 변경 (상태코드를 'a003' 종료로 변경 - 옵션)
		// 또는 별도의 lock 컬럼이 있으면 그것을 사용
		Map<String, Object> lockResult = projectService.lockProject(projectId);
		return lockResult;
	}

}