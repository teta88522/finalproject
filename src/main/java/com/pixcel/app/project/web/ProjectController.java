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
import org.springframework.web.bind.annotation.RequestParam;
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
	public String registerProject(ProjectVO projectVO, @CookieValue(value = "userId", required = false) String userId,
			RedirectAttributes redirectAttributes) {
		if (projectVO.getOwnerId() == null || projectVO.getOwnerId().isEmpty()) {
			projectVO.setOwnerId(userId);
		}

		// ✅ 날짜 검증: startDate <= endDate 확인
		if (projectVO.getStartDate() != null && projectVO.getEndDate() != null) {
			if (projectVO.getStartDate().after(projectVO.getEndDate())) {
				redirectAttributes.addFlashAttribute("errorMessage", "시작일이 완료일보다 늦을 수 없습니다.");
				return "redirect:/myproject/register";
			}
		}

		projectService.registerProject(projectVO);
		return "redirect:/myproject/list"; // ✅ 수정: /project/list → /myproject/list
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

	/* 프로젝트 상세 */
	@GetMapping("/projectdetail/{projectId}")
	public String projectDetail(@PathVariable String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {

		ProjectVO project = projectService.selectProjectDetail(projectId);
		List<ProjectMemberVO> projectMemberList = projectService.selectProjectMemberList(projectId);
		List<IssueStatVO> issueStatList = projectService.selectIssueStatByProjectId(projectId); // ✅ 추가

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectMemberList", projectMemberList);
		model.addAttribute("issueStatList", issueStatList); // ✅ 추가

		return "project/projectDetail";
	}

	/* 구성원 목록 설정 */
	@GetMapping("/project/{projectId}/settings/members")
	public String projectMemberSetting(@PathVariable String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

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
			return "redirect:/projectdetail/" + projectId;

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
			return "redirect:/projectdetail/" + projectId;

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
			return "redirect:/projectdetail/" + projectId;

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
			return "redirect:/projectdetail/" + projectId;

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
			return "redirect:/projectdetail/" + projectId;

		Map<String, Object> resultMap = projectService.deleteProjectMember(projectMemberId);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/members";
	}

	/* 프로젝트 모듈 설정 */
	@GetMapping("/project/{projectId}/settings/modules")
	public String projectModuleSetting(@PathVariable String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn, Model model) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		ProjectVO project = projectService.selectProjectDetail(projectId);
		List<ProjectModulesVO> projectModuleList = projectService.selectAllModuleProjects(projectId);

		model.addAttribute("project", project);
		model.addAttribute("projectId", projectId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("projectModuleList", projectModuleList);

		return "settings/projectModuleSetting";
	}

	/* 프로젝트 모듈 추가 */
	@PostMapping("/project/{projectId}/settings/modules/add")
	public String insertProjectModule(@PathVariable String projectId, @RequestParam String moduleCode,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		Map<String, Object> resultMap = projectService.insertProjectModule(projectId, moduleCode);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/modules";
	}

	/* 프로젝트 모듈 삭제 */
	@PostMapping("/project/{projectId}/settings/modules/delete")
	public String deleteProjectModule(@PathVariable String projectId, @RequestParam String moduleCode,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/projectdetail/" + projectId;

		Map<String, Object> resultMap = projectService.deleteProjectModule(projectId, moduleCode);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/project/" + projectId + "/settings/modules";
	}

	/* 프로젝트 삭제 */
	@PostMapping("/myproject/delete")
	public String deleteProject(@RequestParam String projectId,
			@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
			RedirectAttributes redirectAttributes) {
		if (!"Y".equals(subscribeYn))
			return "redirect:/myproject/list";

		Map<String, Object> resultMap = projectService.deleteProject(projectId);
		redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

		return "redirect:/myproject/list";
	}
}