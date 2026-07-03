package com.pixcel.app.roles.service;

import java.util.List;
import java.util.Map;

public interface roleService {
	
	//권한 목록조회
	public List<permissionServiceVO> selectPermissionList();
	
	//역할 등록
	public Map<String,Object> insertRole(roleServiceVO roleVO, List<String> permissionIds);
	
	//현재 로그인 사용자가 생성한 역할 목록 조회
	public List<roleServiceVO> selectRoleList(roleSearchVO roleSearchVO);
	
	//선택한 역할을 담당하는 구성원 목록 조회
	public List<roleMemberVO> selectRoleMemberList(String roleId);
	
	public roleServiceVO selectRoleById(String roleId);

	public List<String> selectPermissionIdsByRoleId(String roleId);
	
	public Map<String,Object> deleteRole(String roleId);

}
