package com.pixcel.app.project.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectModulesVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;

@Controller
//@RequestMapping("/project")	// 비동기(JSON) 데이터 통신을 위한 컨트롤러 선언
// 프로젝트 관련 기본 URL 경로 설정 (/API는 안붙였는데 나중에 오류 생기면 추가하기)
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
	public String registerProject(ProjectVO projectVO, @CookieValue(value = "userId", required = false) String userId) {
		if (projectVO.getOwnerId() == null || projectVO.getOwnerId().isEmpty()) {
			projectVO.setOwnerId(userId);
		}
		projectService.registerProject(projectVO);
		return "redirect:/project/list";
	}

	// 260623 고동현 추가 - projectList 관련
	// 관리자 = subscribeYn = Y
	// 관리자일 경우 - 본인이 생성한 프로젝트만 조회한다. 또한 프로젝트 생성 및 관리 버튼이 노출된다.
	// 일반이용자 = subscribeYn = N
	// 일반이용자일 경우 - 본인이 소속된 프로젝트가 조회된다. 프로젝트 생성 및 관리 버튼은 미노출된다.

	/* 프로젝트 목록 (검색 + 페이징) */
	@GetMapping("/myproject/list")
	public String projectListForm(Model model, @CookieValue(value = "userId", required = false) String userId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			// 검색 파라미터
			@RequestParam(value = "searchProjectName", required = false) String searchProjectName,
			@RequestParam(value = "searchOwnerName", required = false) String searchOwnerName,
			@RequestParam(value = "searchStatusCode", required = false) String searchStatusCode,
			// 페이징 파라미터
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

		if (subscribeYn == null || subscribeYn.isEmpty()) {
			subscribeYn = "N";
		}

		// 검색+페이징 조건을 ProjectVO에 담아 전달
		ProjectVO searchVO = new ProjectVO();
		searchVO.setOwnerId(userId); // 사용자 식별용
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
		model.addAttribute("searchVO", searchVO); // 페이징·검색 정보 화면 전달

		return "project/projectList";
	}

	/* 프로젝트 상세 */
	@GetMapping("/projectdetail/{projectId}")
	public String projectDetail(@PathVariable String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		ProjectVO project = projectService.selectProjectDetail(projectId);
		List<ProjectMemberVO> projectMemberList = projectService.selectProjectMemberList(projectId);

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectMemberList", projectMemberList);

		return "project/projectDetail";
	}

	/* 구성원 목록 설정 */
	@GetMapping("/project/{projectId}/settings/members")
	public String projectMemberSetting(@PathVariable String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		ProjectVO project = projectService.selectProjectDetail(projectId);
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
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		ProjectVO project = projectService.selectProjectDetail(projectId);
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
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		projectMemberVO.setProjectId(projectId);
		Map<String, Object> resultMap = projectService.insertProjectMember(projectMemberVO);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 구성원 역할 수정 폼 */
	@GetMapping("/project/{projectId}/settings/members/{projectMemberId}/update")
	public String projectMemberUpdateForm(@PathVariable String projectId, @PathVariable String projectMemberId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		ProjectVO project = projectService.selectProjectDetail(projectId);
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
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		projectMemberVO.setProjectId(projectId);
		Map<String, Object> resultMap = projectService.updateProjectMemberRole(projectMemberVO);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 구성원 삭제 */
	@PostMapping("/project/{projectId}/settings/members/delete")
	public String deleteProjectMember(@PathVariable String projectId, @RequestParam String projectMemberId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/project/" + projectId;

		Map<String, Object> resultMap = projectService.deleteProjectMember(projectMemberId);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

}
