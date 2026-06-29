package com.pixcel.app.manage.service;

import java.util.List;
import java.util.Map;

public interface ManageService {
	//관리자가 생성한 프로젝트 목록 조회
	public List<ManageProjectVO> selectMyManageProjectList(String ownerId);
	
	//관리자가 생성한 프로젝트의 그룹 목록
	public List<ManageGroupVO> selectMyManageGroupList(String ownerId);
	
	//관리자가 생성한 역할 목록
	public List<ManageRoleVO> selectMyManageRoleList(String ownerId);
	
	//그룹 생성
	public Map<String,Object> insertManageGroup(ManageGroupVO manageGroupVO, String ownerId);
	
	//그룹 상세 조회
	public ManageGroupVO selectManageGroupDetail(String projectGroupId, String ownerId);
	
	//그룹 수정
	public Map<String, Object> updateManagerGroup(ManageGroupVO manageGroupVO, String owenrId);
	
	//그룹 삭제
	public Map<String, Object> deleteManageGroup(String projectGroupId, String ownerId);
	
	//현재 그룹에 배정된 구성원 목록
	public List<ManageGroupMemberVO> selectAssignedGroupMemberList(String projectGroupId, String ownerId);
	
	// 그룹에 배정 가능한 구성원 목록
	public List<ManageGroupMemberVO> selectAssignableGroupMemberList(String projectGroupId, String ownerId);
	
	// 그룹 구성원 추가 + 개인 역할 지정
	public Map<String, Object> addGroupMember(String projectGroupId, String projectMemberId, String roleId, String ownerId);

	// 그룹 구성원 개인 역할 수정
	public Map<String, Object> updateGroupMemberRole(String projectGroupId, String projectMemberId, String roleId, String ownerId);

	// 그룹 구성원 제외
	public Map<String, Object> removeGroupMember(String projectGroupId, String projectMemberId, String ownerId);
	
	public Map<String, Object> addGroupMemberList(String projectGroupId, List<String> projectMemberIds, List<String> roleIds, String ownerId);
}
