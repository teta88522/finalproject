package com.pixcel.app.roles.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pixcel.app.roles.mapper.roleMapper;
import com.pixcel.app.roles.service.permissionServiceVO;
import com.pixcel.app.roles.service.roleMemberVO;
import com.pixcel.app.roles.service.rolePermissionVO;
import com.pixcel.app.roles.service.roleService;
import com.pixcel.app.roles.service.roleServiceVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class roleServiceImpl implements roleService {
	
	private final roleMapper roleMapper;

	//권한 목록조회
	@Override
	public List<permissionServiceVO> selectPermissionList() {
		return roleMapper.selectPermissionList();
	}

	//역할 등록
	@Override
	@Transactional
	public Map<String, Object> insertRole(roleServiceVO roleVO, List<String> permissionIds) {
		Map<String,Object> resultMap = new HashMap<>();
		
		int result = roleMapper.insertRole(roleVO);
		
		if(result <= 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "역할 등록에 실패했습니다.");
			return resultMap;
		}
		
		if (permissionIds != null && !permissionIds.isEmpty()) {

		    int nextNo = roleMapper.selectRolePermissionNextNo();

		    List<rolePermissionVO> rolePermissionList = new ArrayList<>();

		    String yearMonth = java.time.LocalDate.now()
		            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));

		    for (int i = 0; i < permissionIds.size(); i++) {
		        rolePermissionVO rolePermissionVO = new rolePermissionVO();

		        String rolePermissionId =
		                "ROLE_PERMISSION_"
		                + yearMonth
		                + "_"
		                + String.format("%04d", nextNo + i);

		        rolePermissionVO.setRolePermissionId(rolePermissionId);
		        rolePermissionVO.setRoleId(roleVO.getRoleId());
		        rolePermissionVO.setPermissionId(permissionIds.get(i));

		        rolePermissionList.add(rolePermissionVO);
		    }

		    roleMapper.insertRolePermissionList(rolePermissionList);
		}
		
        resultMap.put("result", true);
        resultMap.put("message", "역할 등록이 완료되었습니다.");
        resultMap.put("roleId", roleVO.getRoleId());

        return resultMap;

	}

	//현재 로그인한 사용자가 생성한 역할 목록 조회
	@Override
	public List<roleServiceVO> selectRoleList(String createdBy) {
		
		 return roleMapper.selectRoleList(createdBy);
	}

	@Override
	public List<roleMemberVO> selectRoleMemberList(String roleId) {
	    return roleMapper.selectRoleMemberList(roleId);
	}
	
	@Override
	public roleServiceVO selectRoleById(String roleId) {
		return roleMapper.selectRoleById(roleId);
	}

	@Override
	public List<String> selectPermissionIdsByRoleId(String roleId) {
		return roleMapper.selectPermissionIdsByRoleId(roleId);
	}

	@Override
	@Transactional
	public Map<String, Object> deleteRole(String roleId) {
		Map<String, Object> resultMap = new HashMap<>();
		
		int memberCount = roleMapper.selectRoleMemberCount(roleId);
		
		if(memberCount > 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "해당 역할을 담당 중인 구성원이 있어 삭제할 수 없습니다.");
			return resultMap;
		}
		roleMapper.deleteRolePermissionByRoleId(roleId);
		
		int deleteResult = roleMapper.deleteRole(roleId);
		
		if(deleteResult <= 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "역할삭제에 실패하였습니다.");
			return resultMap;
		}
		
		resultMap.put("result",true);
		resultMap.put("message", "역할이 삭제되었습니다.");
		return resultMap;
	}
	
}
