package com.pixcel.app.roles.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.roles.service.permissionServiceVO;
import com.pixcel.app.roles.service.roleMemberVO;
import com.pixcel.app.roles.service.rolePermissionVO;
import com.pixcel.app.roles.service.roleSearchVO;
import com.pixcel.app.roles.service.roleServiceVO;

@Mapper
public interface roleMapper {
	
	//역할추가화면 권한 목록조회
	public List<permissionServiceVO> selectPermissionList();
	
	//역할 등록
	public int insertRole(roleServiceVO roleVO);
	
	//역할 권한 등록
	//public int insertRolePermission(rolePermissionVO rolePermissionVO); 권한이 많을 수록 insert하는게 느려져서 아래로 수정합니다.
	
	// role_permission 다음 번호 조회
    public int selectRolePermissionNextNo();

    // role_permission 다건 등록
    public int insertRolePermissionList(@Param("rolePermissionList") List<rolePermissionVO> rolePermissionList);
    
    
    // 현재 로그인 사용자가 생성한 역할 목록 조회
    public List<roleServiceVO> selectRoleList(roleSearchVO roleSearchVO);

    // 선택한 역할을 담당하는 구성원 목록 조회
    public List<roleMemberVO> selectRoleMemberList(@Param("roleId") String roleId);
    
    public roleServiceVO selectRoleById(@Param("roleId") String roleId);

    public List<String> selectPermissionIdsByRoleId(@Param("roleId") String roleId);
    
    // 역할 사용 중인 프로젝트 구성원 수 조회
    public int selectRoleMemberCount(@Param("roleId") String roleId);
    
    // 역할 권한 삭제
    public int deleteRolePermissionByRoleId(@Param("roleId") String roleId);

    // 역할 삭제
    public int deleteRole(@Param("roleId") String roleId);
    
}
