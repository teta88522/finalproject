package com.pixcel.app.project.service;

import java.util.List;
import java.util.Map;

public interface ProjectService {
	public int registerProject(ProjectVO projectVO);

	List<ProjectVO> findAllProjects();
	
	// 프로젝트 모듈 관련
	public List<ProjectModulesVO> selectAllModuleProjects(String projectId);

	// 프로젝트 리스트 관련
	// 관리자 (검색+페이징 적용)
	public List<ProjectVO> selectMyCreatedProjectList(ProjectVO projectVO);

	// 일반이용자 (검색+페이징 적용)
	public List<ProjectVO> selectMyJoinedProjectList(ProjectVO projectVO);

	public ProjectVO selectProjectDetail(String projectId);

	public List<ProjectMemberVO> selectProjectMemberList(String projectId);

	public List<ProjectMemberVO> selectProjectMemberCandidateList(String projectId);

	public List<ProjectRoleVO> selectProjectRoleList(String projectId);

	public Map<String, Object> insertProjectMember(ProjectMemberVO projectMemberVO);

	public ProjectMemberVO selectProjectMemberDetail(String projectMemberId);

	public Map<String, Object> updateProjectMemberRole(ProjectMemberVO projectMemberVO);

	public Map<String, Object> deleteProjectMember(String projectMemberId);
	
	public Map<String, Object> deleteProject(String projectId);
	
	// ✅ 추가: 일감 추적 데이터 조회
	public List<IssueStatVO> selectIssueStatByProjectId(String projectId);
}