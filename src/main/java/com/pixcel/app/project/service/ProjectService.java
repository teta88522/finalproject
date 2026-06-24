package com.pixcel.app.project.service;

import java.util.List;
import java.util.Map;

public interface ProjectService {
	// 프로젝트 등록 프로세스(뼈대)
	public int registerProject(ProjectVO projectVO);
	
	// 자료실에서 사용할 프로젝트 목록을 가져오는 메서드
	List<ProjectVO> findAllProjects();
	
	//260623 고동현 추가 프로젝트 리스트관련
	//관리자 본인이 생성한 프로젝트 리스트 조회
	public List<ProjectVO> selectMyCreatedProjectList(String userId);
	//일반이용자 소속된 프로젝트 리스트 조회
	public List<ProjectVO> selectMyJoinedProjectList(String userId);
	// 프로젝트 상세 조회
	public ProjectVO selectProjectDetail(String projectId);
	
	//프로젝트 설정 - 구성원 목록 조회
	public List<ProjectMemberVO> selectProjectMemberList(String projectId);
	
	//프로젝트 구성원 추가 시 후보 목록 조회
	public List<ProjectMemberVO> selectProjectMemberCandidateList(String projectId);
	
	//프로젝트 역할 목록 조회
	public List<ProjectRoleVO> selectProjectRoleList(String projectId);
	
	//프로젝트 구성원 등록
	public Map<String, Object> insertProjectMember(ProjectMemberVO projectMemberVO);
	
	//구성원 단건 조회
	public ProjectMemberVO selectProjectMemberDetail(String projectMemberId);
	
	//프로젝트 구성원 역할 수정
	public Map<String,Object> updateProjectMemberRole(ProjectMemberVO projectMemberVO);
	
	//프로젝트 구성원 삭제
	public Map<String, Object> deleteProjectMember(String projectMemberId);
}
