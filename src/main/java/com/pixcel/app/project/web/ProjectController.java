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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;

@Controller
@RequestMapping("/project")	// 비동기(JSON) 데이터 통신을 위한 컨트롤러 선언
// 프로젝트 관련 기본 URL 경로 설정 (/API는 안붙였는데 나중에 오류 생기면 추가하기)
public class ProjectController {
	
	@Autowired
	private ProjectService projectService;
	
	/*
	 * 신규 프로젝트 및 선택 모듈 등록 API 
	 * @param projectVO 화면(vue)에서 JSON 형태로 넘어오는 프로젝트 데이터
	 * @return 등록 성공한 행(row)의 총 개수
	 */
	
	@GetMapping("/register")
	public String registerForm() {
		// src/main/resources/templates/project/register.html 파일을 찾아감
		return "project/project";	// 팀 폴더 구조에 맞게 경로 수정
	}
	
	// 프로젝트 등록 실행 (데이터 저장 처리)
	@PostMapping("/register")
	@ResponseBody
	public int registerProject(ProjectVO projectVO) {
		return projectService.registerProject(projectVO);
	}
	
	
	//260623 고동현 추가 - projectList 관련
	// 관리자 = subscribeYn = Y
	// 관리자일 경우 - 본인이 생성한 프로젝트만 조회한다. 또한 프로젝트 생성 및 관리 버튼이 노출된다.
	// 일반이용자 = subscribeYn = N
	// 일반이용자일 경우 - 본인이 소속된 프로젝트가 조회된다. 프로젝트 생성 및 관리 버튼은 미노출된다.
	
	@GetMapping("/list")
	public String projectListForm(Model model, @CookieValue(value = "userId", required=false)String userId,
											   @CookieValue(value="subscribeYn", required=false)String subscribeYn) {
		
		List<ProjectVO> projectList;
		//sout for test
		System.out.println("현재 로그인 userId = [" + userId + "]");
        System.out.println("현재 로그인 subscribeYn = [" + subscribeYn + "]");
		
        //로그인 하지 않았거나, 쿠기가 없을 경우 기본 N으로 처리한다.
        if(subscribeYn == null || subscribeYn.equals("")) {
        	subscribeYn = "N";
        }
        
        //관리자
        if("Y".equals(subscribeYn)) {
        	projectList = projectService.selectMyCreatedProjectList(userId);
        }
        //일반 이용자
        else {
        	projectList = projectService.selectMyJoinedProjectList(userId);
        }
        
        model.addAttribute("projectList",projectList);
        model.addAttribute("subscribeYn", subscribeYn);
        
		return "project/projectList";
	}
	
	//상세페이지 껍데기만 생성해뒀음 추후 수정필요
	@GetMapping("/{projectId}")
	public String projectDetail(@PathVariable String projectId,
								@CookieValue(value = "subscribeYn",required = false) String subscribeYn,
								Model model) {
		ProjectVO project = projectService.selectProjectDetail(projectId);
		
		model.addAttribute("project",project);
		model.addAttribute("projectId",projectId);
		model.addAttribute("subscribeYn",subscribeYn);
		return "project/projectDetail";
	}
	
	//프로젝트 설정 - 구성원 목록조회
	@GetMapping("/{projectId}/settings/members")
	public String projectMemberSetting(@PathVariable String projectId,
									   @CookieValue(value ="subscribeYn", required = false)String subscribeYn,
									   Model model) {
		
		//관리자만 접근가능
		if(!"Y".equals(subscribeYn)) {
			return "redirect:/project/"+projectId;
		}
		
		ProjectVO project = projectService.selectProjectDetail(projectId);
		List<ProjectMemberVO> projectMemberList = projectService.selectProjectMemberList(projectId);
		
		model.addAttribute("project",project);
		model.addAttribute("projectId",projectId);
		model.addAttribute("subscribeYn",subscribeYn);
		model.addAttribute("projectMemberList",projectMemberList);
		
		return "settings/projectMemberSetting";
	}
	
	//구성원 추가 페이지
	@GetMapping("/{projectId}/settings/members/new")
	public String projectMemberAddForm(@PathVariable String projectId,
	                                   @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                                   Model model) {

	    // 관리자만 접근 가능
	    if (!"Y".equals(subscribeYn)) {
	        return "redirect:/project/" + projectId;
	    }

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

	//구성원 추가 기능
	@PostMapping("/{projectId}/settings/members/add")
	public String insertProjectMember(@PathVariable String projectId,
	                                  ProjectMemberVO projectMemberVO,
	                                  @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                                  RedirectAttributes redirectAttributes) {

	    // 관리자만 처리 가능
	    if (!"Y".equals(subscribeYn)) {
	        return "redirect:/project/" + projectId;
	    }

	    projectMemberVO.setProjectId(projectId);

	    Map<String, Object> resultMap = projectService.insertProjectMember(projectMemberVO);

	    redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

	    return "redirect:/project/" + projectId + "/settings/members";
	}
	
	//멤버 권한 수정 페이지
	@GetMapping("/{projectId}/settings/members/{projectMemberId}/update")
	public String projectMemberUpdateForm(@PathVariable String projectId,
										  @PathVariable String projectMemberId,
										  @CookieValue(value="subscribeYn",required = false) String subscribeYn,
										  Model model) {
		
		if(!"Y".equals(subscribeYn)) {
			return "redirect:/project/" + projectId;
		}
		
		ProjectVO project = projectService.selectProjectDetail(projectId);
		ProjectMemberVO projectMember = projectService.selectProjectMemberDetail(projectMemberId);
		List<ProjectRoleVO> projectRoleList = projectService.selectProjectRoleList(projectId); 
		
		model.addAttribute("project",project);
		model.addAttribute("projectId",projectId);
		model.addAttribute("subscribeYn",subscribeYn);
		model.addAttribute("projectMember",projectMember);
		model.addAttribute("projectRoleList",projectRoleList);
		
		return "settings/projectMemberUpdate";
		
	}
	
	//멤버 권한 수정
	@PostMapping("/{projectId}/settings/members/update")
	public String updateProjectMemberRole(@PathVariable String projectId,
	                                      ProjectMemberVO projectMemberVO,
	                                      @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                                      RedirectAttributes redirectAttributes) {

	    if (!"Y".equals(subscribeYn)) {
	        return "redirect:/project/" + projectId;
	    }

	    projectMemberVO.setProjectId(projectId);

	    Map<String, Object> resultMap = projectService.updateProjectMemberRole(projectMemberVO);

	    redirectAttributes.addFlashAttribute("message", resultMap.get("message"));
	    
	    //redirectAttributes.addFlashAttribute = 일회성 model;

	    return "redirect:/project/" + projectId + "/settings/members";
	}

	//구성원 삭제
	@PostMapping("/{projectId}/settings/members/delete")
	public String deleteProjectMember(@PathVariable String projectId,
	                                  @RequestParam String projectMemberId,
	                                  @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                                  RedirectAttributes redirectAttributes) {

	    if (!"Y".equals(subscribeYn)) {
	        return "redirect:/project/" + projectId;
	    }

	    Map<String, Object> resultMap = projectService.deleteProjectMember(projectMemberId);

	    redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

	    return "redirect:/project/" + projectId + "/settings/members";
	}
	
	
	
}
