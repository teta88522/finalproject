package com.pixcel.app.manage.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.manage.service.ManageGroupMemberVO;
import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;
import com.pixcel.app.manage.service.ManageRoleVO;
import com.pixcel.app.manage.service.ManageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage")
public class ManageController {

    private final ManageService manageService;

    @GetMapping("/groups")
    public String manageGroupList(
            @CookieValue(value = "userId", required = false) String userId,
            @CookieValue(value = "user_pk", required = false) String userPk,
            @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
            Model model) {

        // 구독 승인 관리자만 접근 가능
        if (!"Y".equals(subscribeYn)) {
            return "redirect:/";
        }

        // 프로젝트 owner_id로 쓸 로그인 사용자 PK
        String ownerId = userId;

        if (ownerId == null || ownerId.equals("")) {
            ownerId = userPk;
        }

        if (ownerId == null || ownerId.equals("")) {
            return "redirect:/login";
        }

        List<ManageProjectVO> myProjectList = manageService.selectMyManageProjectList(ownerId);
        List<ManageGroupVO> manageGroupList = manageService.selectMyManageGroupList(ownerId);

        model.addAttribute("subscribeYn", subscribeYn);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("myProjectList", myProjectList);
        model.addAttribute("manageGroupList", manageGroupList);

        return "manage/group/manageGroupList";
    }
    
    private String getLoginOwnerId(String userId, String userPk) {

        if (userId != null && !userId.equals("")) {
            return userId;
        }

        if (userPk != null && !userPk.equals("")) {
            return userPk;
        }

        return null;
    }
    
    @GetMapping("/groups/new")
    public String manageGroupAddForm(@CookieValue(value ="userId", required = false) String userId,
    								 @CookieValue(value = "user_pk", required = false) String userPk,
    								 @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
    								 Model model) {
    	
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/project";
    	}
    	String ownerId = getLoginOwnerId(userId,userPk);
    	
    	if(ownerId == null ||ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	List<ManageProjectVO> myProjectList = manageService.selectMyManageProjectList(ownerId);
    	List<ManageRoleVO> manageRoleList = manageService.selectMyManageRoleList(ownerId);
    	
    	model.addAttribute("subscribeYn",subscribeYn);
    	model.addAttribute("ownerId",ownerId);
    	model.addAttribute("myProjectList", myProjectList);
    	model.addAttribute("manageRoleList", manageRoleList);
    	model.addAttribute("manageGroupVO",new ManageGroupVO());
    	
    	return "manage/group/manageGroupAdd";
    }
    
    @PostMapping("/groups/add")
    public String insertManageGroup(ManageGroupVO manageGroupVO,
    								@CookieValue(value = "userId",required = false) String userId,
    								@CookieValue(value = "user_pk", required=false) String userPk,
    								@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
    								RedirectAttributes redirectAttributes) {
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/";
    	}
    	
    	String ownerId = getLoginOwnerId(userId, userPk);
    	
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	Map<String, Object> resultMap = manageService.insertManageGroup(manageGroupVO, ownerId);
    	redirectAttributes.addFlashAttribute("message", resultMap.get("message"));
    	
    	if(Boolean.TRUE.equals(resultMap.get("result"))) {
    		return "redirect:/manage/groups";
    	}
    	
    	return "redirect:/manage/groups/new";
    }
    
    
    @GetMapping("/groups/{projectGroupId}")
    public String manageGroupDetail(@PathVariable String projectGroupId,
    								@CookieValue(value = "userId", required = false) String userId,
    								@CookieValue(value = "user_pk",required = false) String userPk,
    								@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
    								Model model) {
    	
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/project";
    	}
    	
    	String ownerId = getLoginOwnerId(userId, userPk);
    	
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	ManageGroupVO manageGroup = manageService.selectManageGroupDetail(projectGroupId, ownerId);
    	
    	if(manageGroup == null) {
    		return "redirect:/manage/groups";
    	}
    	
    	List<ManageRoleVO> manageRoleList = manageService.selectMyManageRoleList(ownerId);
    	
    	List<ManageGroupMemberVO> assignedMemberList = manageService.selectAssignedGroupMemberList(projectGroupId, ownerId);
    	
    	model.addAttribute("subscribeYn",subscribeYn);
    	model.addAttribute("ownerId",ownerId);
    	model.addAttribute("manageGroup",manageGroup);
    	model.addAttribute("manageRoleList",manageRoleList);
    	 model.addAttribute("assignedMemberList", assignedMemberList);
    	 
    	return "manage/group/manageGroupDetail";
    }
    
    @PostMapping("/groups/update")
    public String updateManageGroup(ManageGroupVO manageGroupVO, 
    								@CookieValue(value = "userId",required = false) String userId,
    								@CookieValue(value = "user_pk",required = false) String userPk,
    								@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
    								RedirectAttributes redirectAttributes) {
    	
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/";
    	}
    	
    	String ownerId = getLoginOwnerId(userId, userPk);
    	
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	Map<String,Object> resultMap = manageService.updateManagerGroup(manageGroupVO, ownerId);
    	
    	redirectAttributes.addFlashAttribute("message",resultMap.get("messeage"));
    	
    	if(Boolean.TRUE.equals(resultMap.get("result"))) {
    		 return "redirect:/manage/groups";
    	}
    	return "redirect:/manage/groups";
    }
    
    @PostMapping("/groups/delete")
    public String deleteManageGroup(@RequestParam String projectGroupId,
    								@CookieValue(value = "userId", required = false) String userId,
    								@CookieValue(value = "user_pk", required = false) String userPk,
    								@CookieValue(value = "subscribeYn", required = false) String subscribeYn,
    								 RedirectAttributes redirectAttributes) {
    	
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/";
    	}
    	
    	String ownerId = getLoginOwnerId(userId, userPk);
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	Map<String,Object> resultMap = manageService.deleteManageGroup(projectGroupId, ownerId);
    	redirectAttributes.addFlashAttribute("message",resultMap.get("message"));
    	
    	return "redirect:/manage/groups";
    }
    
    @GetMapping("/groups/{projectGroupId}/members")
    public String manageGroupMemberPage(@PathVariable String projectGroupId,
                                        @CookieValue(value = "userId", required = false) String userId,
                                        @CookieValue(value = "user_pk", required = false) String userPk,
                                        @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
                                        Model model) {

        if (!"Y".equals(subscribeYn)) {
            return "redirect:/project";
        }

        String ownerId = getLoginOwnerId(userId, userPk);

        if (ownerId == null || ownerId.equals("")) {
            return "redirect:/login";
        }

        // 그룹 상세 정보
        ManageGroupVO manageGroup = manageService.selectManageGroupDetail(projectGroupId, ownerId);

        if (manageGroup == null) {
            return "redirect:/manage/groups";
        }

        // 역할 목록
        List<ManageRoleVO> manageRoleList = manageService.selectMyManageRoleList(ownerId);

        // 그룹에 배정 가능한 구성원 목록
        List<ManageGroupMemberVO> assignableMemberList =
                manageService.selectAssignableGroupMemberList(projectGroupId, ownerId);

        // 현재 그룹에 배정된 구성원 목록
        List<ManageGroupMemberVO> assignedMemberList =
                manageService.selectAssignedGroupMemberList(projectGroupId, ownerId);

        model.addAttribute("subscribeYn", subscribeYn);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("manageGroup", manageGroup);
        model.addAttribute("manageRoleList", manageRoleList);
        model.addAttribute("assignableMemberList", assignableMemberList);
        model.addAttribute("assignedMemberList", assignedMemberList);

        return "manage/group/manageGroupMember";
    }
    
//    @PostMapping("/groups/{projectGroupId}/members/add")
//    public String addGroupMember(@PathVariable String projectGroupId,
//    							 @RequestParam String projectMemberId,
//    							 @RequestParam String roleId,
//    							 @CookieValue(value ="userId", required = false) String userId,
//    							 @CookieValue(value = "user_pk", required = false) String userPk,
//    							 @CookieValue(value ="subscribeYn",required = false) String subscribeYn,
//    							 RedirectAttributes redirectAttributes) {
//    	if(!"Y".equals(subscribeYn)) {
//    		return "redirect:/";
//    	}
//    	
//    	String ownerId = getLoginOwnerId(userId, userPk);
//    	
//    	if(ownerId == null || ownerId.equals("")) {
//    		return "redirect:/login";
//    	}
//    	
//    	Map<String , Object> resultMap = manageService.addGroupMember(projectGroupId, projectMemberId, roleId, ownerId);
//    	
//    	redirectAttributes.addFlashAttribute("message",resultMap.get("message"));
//    	
//    	return "redirect:/manage/groups/" + projectGroupId + "/members";
//    }
    @PostMapping("/groups/{projectGroupId}/members/add")
    public String addGroupMember(@PathVariable String projectGroupId,
                                 @RequestParam(value = "projectMemberIds", required = false) List<String> projectMemberIds,
                                 @RequestParam(value = "roleIds", required = false) List<String> roleIds,
                                 @CookieValue(value = "userId", required = false) String userId,
                                 @CookieValue(value = "user_pk", required = false) String userPk,
                                 @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
                                 RedirectAttributes redirectAttributes) {

        if (!"Y".equals(subscribeYn)) {
            return "redirect:/";
        }

        String ownerId = getLoginOwnerId(userId, userPk);

        if (ownerId == null || ownerId.equals("")) {
            return "redirect:/login";
        }

        if (projectMemberIds == null || projectMemberIds.isEmpty()
                || roleIds == null || roleIds.isEmpty()) {

            redirectAttributes.addFlashAttribute("message", "선택한 구성원이 없습니다.");
            return "redirect:/manage/groups/" + projectGroupId + "/members";
        }

        if (projectMemberIds.size() != roleIds.size()) {
            redirectAttributes.addFlashAttribute("message", "구성원과 역할 정보가 일치하지 않습니다.");
            return "redirect:/manage/groups/" + projectGroupId + "/members";
        }

        Map<String, Object> resultMap =
                manageService.addGroupMemberList(projectGroupId, projectMemberIds, roleIds, ownerId);

        redirectAttributes.addFlashAttribute("message", resultMap.get("message"));

        if (Boolean.TRUE.equals(resultMap.get("result"))) {
            return "redirect:/manage/groups/" + projectGroupId;
        }

        return "redirect:/manage/groups/" + projectGroupId + "/members";
    }
    
    @PostMapping("/groups/{projectGroupId}/members/role/update")
    public String updateGroupMemberRole(@PathVariable String projectGroupId,
    									@RequestParam String projectMemberId,
    									@RequestParam String roleId,
    									@CookieValue(value ="userId", required = false) String userId,
    									@CookieValue(value ="user_pk", required = false) String userPk,
    									@CookieValue(value ="subscribeYn",required = false) String subscribeYn,
    									RedirectAttributes redirectAttributes) {
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/";
    	}
    	String ownerId = getLoginOwnerId(userId, userPk);
    	
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	Map<String, Object> resultMap = manageService.updateGroupMemberRole(projectGroupId, projectMemberId, roleId, ownerId);
    	
    	redirectAttributes.addFlashAttribute("message",resultMap.get("message"));
    	return "redirect:/manage/groups/" + projectGroupId;
    };
    
    @PostMapping("/groups/{projectGroupId}/members/remove")
    public String removeGroupMember(@PathVariable String projectGroupId,
    								@RequestParam String projectMemberId,
    								@CookieValue(value ="userId", required = false) String userId,
    								@CookieValue(value="user_pk", required = false) String userPk,
    								@CookieValue(value="subscribeYn", required = false) String subscribeYn,
    								RedirectAttributes redirectAttributes) {
    	
    	if(!"Y".equals(subscribeYn)) {
    		return "redirect:/";
    	}
    	
    	String ownerId = getLoginOwnerId(userId, userPk);
    	if(ownerId == null || ownerId.equals("")) {
    		return "redirect/login";
    	}
    	
    	Map<String, Object> resultMap = manageService.removeGroupMember(projectGroupId, projectMemberId, ownerId);
    	
    	redirectAttributes.addFlashAttribute("message", resultMap.get("message"));
    	
    	return "redirect:/manage/groups/" + projectGroupId + "/members" ;
    };
    
    
}