package com.pixcel.app.roles.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pixcel.app.roles.service.permissionServiceVO;
import com.pixcel.app.roles.service.roleMemberVO;
import com.pixcel.app.roles.service.roleService;
import com.pixcel.app.roles.service.roleServiceVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RolesController {
	
	private final roleService roleService;
	
	//역할 목록 화면
	@GetMapping("/roles")
	public String rolesListForm(Model model, @CookieValue(value = "userId", required = false) String userId) {
		List<roleServiceVO> roleList = roleService.selectRoleList(userId);

		model.addAttribute("roleList",roleList);
		
		return "roles/rolesList";
	}
	
	//역할 목록 구성원 조회
	@GetMapping("/roles/member")
	@ResponseBody
	public List<roleMemberVO> roleMemberList(@RequestParam String roleId) {
		System.out.println("선택한 roleId = [" + roleId + "]");

	    List<roleMemberVO> memberList = roleService.selectRoleMemberList(roleId);

	    System.out.println("memberList = " + memberList);
	    System.out.println("memberList size = " + memberList.size());

	    return memberList;
	}
	
	//역할 추가 화면
	@GetMapping("/roles/new")
	public String roleInsertForm(Model model,
	                             @RequestParam(required = false) String copyRoleId) {

	    List<permissionServiceVO> permissionList = roleService.selectPermissionList();

	    roleServiceVO roleVO = new roleServiceVO();
	    List<String> checkedPermissionIds = new java.util.ArrayList<>();

	    if (copyRoleId != null && !copyRoleId.equals("")) {

	        roleVO = roleService.selectRoleById(copyRoleId);

	        if (roleVO != null) {
	            roleVO.setRoleId(null);
	            roleVO.setRoleName(roleVO.getRoleName() + "_복사본");
	        }

	        checkedPermissionIds = roleService.selectPermissionIdsByRoleId(copyRoleId);
	    }


	    model.addAttribute("roleServiceVO", roleVO);
	    model.addAttribute("permissionList", permissionList);
	    model.addAttribute("checkedPermissionIds", checkedPermissionIds);

	    return "roles/rolesAdd";
	}
	
	//역할 추가 등록
	@PostMapping("/roles/add")
	public String insertRole(roleServiceVO roleVO, @RequestParam(required = false) List<String> permissionIds, @CookieValue(value = "userId", required = false)String userId) {
		roleVO.setCreatedBy(userId);
		roleService.insertRole(roleVO, permissionIds);
		return "redirect:/roles"; 
	}
	
	//역할 삭제
	@PostMapping("/roles/delete")
	@ResponseBody
	public Map<String, Object> deleteRole(@RequestParam String roleId){
		
		Map<String,Object> resultMap = roleService.deleteRole(roleId);
		return resultMap;
	}
	
	
}
