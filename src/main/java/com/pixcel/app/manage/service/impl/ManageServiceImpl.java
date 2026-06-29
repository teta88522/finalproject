package com.pixcel.app.manage.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.manage.mapper.ManageMapper;
import com.pixcel.app.manage.service.ManageGroupMemberVO;
import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;
import com.pixcel.app.manage.service.ManageRoleVO;
import com.pixcel.app.manage.service.ManageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManageServiceImpl implements ManageService {
	
	private final ManageMapper manageMapper;
	
	@Override
	public List<ManageProjectVO> selectMyManageProjectList(String ownerId) {
		return manageMapper.selectMyManageProjectList(ownerId);
	}

	@Override
	public List<ManageGroupVO> selectMyManageGroupList(String ownerId) {
		return manageMapper.selectMyManageGroupList(ownerId);
	}

	@Override
	public List<ManageRoleVO> selectMyManageRoleList(String ownerId) {
		return manageMapper.selectMyManageRoleList(ownerId);
	}

	@Override
	@Transactional
	public Map<String, Object> insertManageGroup(ManageGroupVO manageGroupVO, String ownerId) {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		//프로젝트 소유자 검증
		int projectOwnerCount = manageMapper.selectProjectOwnerCheckCount(manageGroupVO.getProjectId(), ownerId);
		
		if(projectOwnerCount <= 0) {
			resultMap.put("result", false);
			resultMap.put("message", "본인이 생성한 프로젝트에만 그룹을 생성할 수 있습니다.");
			return resultMap;
		}
		
		//같은 프로젝트에서 동일 그룹명 방지
		int duplicateCount = manageMapper.selectManageGroupDuplicateCount(manageGroupVO.getProjectId(), manageGroupVO.getGroupName());
		
		if(duplicateCount > 0) {
			resultMap.put("result", false);
			resultMap.put("message", "이미 같은 이름의 그룹이 존재합니다.");
			return resultMap;
		}
		
		int insertResult = manageMapper.insertManageGroup(manageGroupVO);
		
		if(insertResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message","그룹 생성에 실패하였습니다.");
			return resultMap;
		}
		
		resultMap.put("result",true);
		resultMap.put("message","그룹이 생성되었습니다.");
		resultMap.put("projectGroupId", manageGroupVO.getProjectGroupId());
		return resultMap;
	}

	@Override
	public ManageGroupVO selectManageGroupDetail(String projectGroupId, String ownerId) {
		return manageMapper.selectManageGroupDetail(projectGroupId, ownerId);
	}

	@Override
	@Transactional
	public Map<String, Object> updateManagerGroup(ManageGroupVO manageGroupVO, String owenrId) {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		ManageGroupVO originGroup = manageMapper.selectManageGroupDetail(manageGroupVO.getProjectGroupId(), owenrId);
		
		if(originGroup == null) {
			resultMap.put("result",false);
			resultMap.put("message","수정할 그룹을 찾을 수 없습니다.");
		}
		
		int duplicateCount  = 0 ;
		
		//그룹명을 바꾼 경우에만 중복검사
		if(!originGroup.getGroupName().equals(manageGroupVO.getGroupName())) {
			duplicateCount = manageMapper.selectManageGroupDuplicateCount(originGroup.getProjectId(), manageGroupVO.getGroupName());
		}
		
		if(duplicateCount > 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "이미 같은 이름의 그룹이 존재합니다.");
			return resultMap;
		}
		
		//기존 projectId유지
		manageGroupVO.setProjectId(originGroup.getProjectId());
		int updateResult = manageMapper.updateManageGroup(manageGroupVO);
		
		if(updateResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message", "그룹 수정에 실패하였습니다.");
			return resultMap;
		}
		
		resultMap.put("result",true);
		resultMap.put("message", "그룹이 수정되었습니다.");
		
		return resultMap;
	}

	@Override
	@Transactional
	public Map<String, Object> deleteManageGroup(String projectGroupId, String ownerId) {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		ManageGroupVO manageGroup = manageMapper.selectManageGroupDetail(projectGroupId, ownerId);
		
		if(manageGroup == null) {
			resultMap.put("result", false);
			resultMap.put("message","삭제할 그룹을 찾을 수 없습니다.");
			return resultMap;
		}
		
		int memberCount = manageMapper.selectGroupMemberCount(projectGroupId);
		
		if(memberCount > 0) {
			resultMap.put("result", false);
			resultMap.put("message","그룹에 소속된 구성원이 있어 삭제할 수 없습니다.");
			return resultMap;
		}
		
		int deleteResult = manageMapper.deleteManageGroup(projectGroupId, ownerId);
		
		if(deleteResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message","그룹 삭제에 실패하였습니다.");
			return resultMap;
		}
		
		resultMap.put("result", true);
		resultMap.put("message","그룹이 삭제되었습니다.");
		
		return resultMap;
	}

	@Override
	public List<ManageGroupMemberVO> selectAssignedGroupMemberList(String projectGroupId, String ownerId) {
		return manageMapper.selectAssignedGroupMemberList(projectGroupId, ownerId);
	}

	@Override
	public List<ManageGroupMemberVO> selectAssignableGroupMemberList(String projectGroupId, String ownerId) {
		return manageMapper.selectAssignableGroupMemberList(projectGroupId, ownerId);
	}

	@Override
	@Transactional
	public Map<String, Object> addGroupMember(String projectGroupId, String projectMemberId, String roleId, String ownerId) {

	    Map<String, Object> resultMap = new HashMap<>();

	    if (projectGroupId == null || projectGroupId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹 정보가 없습니다.");
	        return resultMap;
	    }

	    if (projectMemberId == null || projectMemberId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 정보가 없습니다.");
	        return resultMap;
	    }

	    if (roleId == null || roleId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 역할을 선택하세요.");
	        return resultMap;
	    }

	    ManageGroupVO manageGroup = manageMapper.selectManageGroupDetail(projectGroupId, ownerId);

	    if (manageGroup == null) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹을 찾을 수 없습니다.");
	        return resultMap;
	    }

	    int result = manageMapper.addGroupMember(projectGroupId, projectMemberId, roleId, ownerId);

	    if (result <= 0) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 배정에 실패했습니다. 이미 다른 그룹에 배정된 구성원일 수 있습니다.");
	        return resultMap;
	    }

	    resultMap.put("result", true);
	    resultMap.put("message", "구성원이 그룹에 배정되었습니다.");

	    return resultMap;
	}


	@Override
	@Transactional
	public Map<String, Object> updateGroupMemberRole(String projectGroupId, String projectMemberId, String roleId, String ownerId) {

	    Map<String, Object> resultMap = new HashMap<>();

	    if (projectGroupId == null || projectGroupId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹 정보가 없습니다.");
	        return resultMap;
	    }

	    if (projectMemberId == null || projectMemberId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 정보가 없습니다.");
	        return resultMap;
	    }

	    if (roleId == null || roleId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 역할을 선택하세요.");
	        return resultMap;
	    }

	    ManageGroupVO manageGroup = manageMapper.selectManageGroupDetail(projectGroupId, ownerId);

	    if (manageGroup == null) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹을 찾을 수 없습니다.");
	        return resultMap;
	    }

	    int result = manageMapper.updateGroupMemberRole(projectGroupId, projectMemberId, roleId, ownerId);

	    if (result <= 0) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 역할 수정에 실패했습니다.");
	        return resultMap;
	    }

	    resultMap.put("result", true);
	    resultMap.put("message", "구성원 역할이 수정되었습니다.");

	    return resultMap;
	}


	@Override
	@Transactional
	public Map<String, Object> removeGroupMember(String projectGroupId, String projectMemberId, String ownerId) {

	    Map<String, Object> resultMap = new HashMap<>();

	    if (projectGroupId == null || projectGroupId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹 정보가 없습니다.");
	        return resultMap;
	    }

	    if (projectMemberId == null || projectMemberId.equals("")) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 정보가 없습니다.");
	        return resultMap;
	    }

	    ManageGroupVO manageGroup = manageMapper.selectManageGroupDetail(projectGroupId, ownerId);

	    if (manageGroup == null) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹을 찾을 수 없습니다.");
	        return resultMap;
	    }

	    int result = manageMapper.removeGroupMember(projectGroupId, projectMemberId, ownerId);

	    if (result <= 0) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원 제외에 실패했습니다.");
	        return resultMap;
	    }

	    resultMap.put("result", true);
	    resultMap.put("message", "구성원이 그룹에서 제외되었습니다.");

	    return resultMap;
	}

	@Override
	@Transactional
	public Map<String, Object> addGroupMemberList(String projectGroupId,
	                                              List<String> projectMemberIds,
	                                              List<String> roleIds,
	                                              String ownerId) {

	    Map<String, Object> resultMap = new HashMap<>();

	    if (projectMemberIds == null || projectMemberIds.isEmpty()
	            || roleIds == null || roleIds.isEmpty()) {

	        resultMap.put("result", false);
	        resultMap.put("message", "선택한 구성원이 없습니다.");
	        return resultMap;
	    }

	    if (projectMemberIds.size() != roleIds.size()) {
	        resultMap.put("result", false);
	        resultMap.put("message", "구성원과 역할 정보가 일치하지 않습니다.");
	        return resultMap;
	    }

	    ManageGroupVO manageGroup = selectManageGroupDetail(projectGroupId, ownerId);

	    if (manageGroup == null) {
	        resultMap.put("result", false);
	        resultMap.put("message", "그룹 정보를 찾을 수 없습니다.");
	        return resultMap;
	    }

	    for (int i = 0; i < projectMemberIds.size(); i++) {
	        String projectMemberId = projectMemberIds.get(i);
	        String roleId = roleIds.get(i);

	        Map<String, Object> addResult =
	                addGroupMember(projectGroupId, projectMemberId, roleId, ownerId);

	        if (!Boolean.TRUE.equals(addResult.get("result"))) {
	            throw new RuntimeException(String.valueOf(addResult.get("message")));
	        }
	    }

	    resultMap.put("result", true);
	    resultMap.put("message", "구성원 배정이 완료되었습니다.");

	    return resultMap;
	}

}
