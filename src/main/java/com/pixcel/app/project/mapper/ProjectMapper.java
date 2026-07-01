package com.pixcel.app.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.project.service.IssueStatVO;
import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectModulesVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectVO;

@Mapper
public interface ProjectMapper {
	
	// 프로젝트 등록
	public int insertProject(ProjectVO projectVO);
	public int insertProjectModules(ProjectVO projectVO);
 
	// 자료실
	public List<ProjectVO> selectAllProjects();
	// 류송지 추가
	// 프로젝트 모듈 관련
	public List<ProjectModulesVO> selectAllModuleProjects(String projectId);
 
	//260623 고동현 프로젝트 리스트 관련추가
	// 관리자 - 본인 생성 목록 (검색+페이징)
	public List<ProjectVO> selectMyCreatedProjectList(ProjectVO projectVO);
	public int selectMyCreatedProjectCount(ProjectVO projectVO);
 
	// 일반이용자 - 소속 목록 (검색+페이징)
	public List<ProjectVO> selectMyJoinedProjectList(ProjectVO projectVO);
	public int selectMyJoinedProjectCount(ProjectVO projectVO);
 
	// 상세 조회
	public ProjectVO selectProjectDetail(@Param("projectId") String projectId);
 
	// 구성원
	public List<ProjectMemberVO> selectProjectMemberList(@Param("projectId") String projectId);
	public List<ProjectMemberVO> selectProjectMemberCandidateList(@Param("projectId") String projectId);
	public List<ProjectRoleVO>   selectProjectRoleList(@Param("projectId") String projectId);
	public int selectProjectMemberDuplicateCount(@Param("projectId") String projectId,
	                                              @Param("teamMemberId") String teamMemberId);
	public int selectProjectMemberNextNo();
	public int insertProjectMember(ProjectMemberVO projectMemberVO);
	public ProjectMemberVO selectProjectMemberDetail(@Param("projectMemberId") String projectMemberId);
	public int updateProjectMemberRole(ProjectMemberVO projectMemberVO);
	public int deleteProjectMember(@Param("projectMemberId") String projectMemberId);
	
	// 프로젝트 삭제 쿼리 추가
	public int deleteProject(@Param("projectId") String projectId);
	void deleteProjectModulesByProjectId(String projectId);
	void deleteProjectMembersByProjectId(String projectId);
	
	public List<IssueStatVO> selectIssueStatByProjectId(@Param("projectId") String projectId);
	
	public int insertProjectModule(@Param("projectId") String projectId, @Param("moduleCode") String moduleCode);
	public int deleteProjectModuleByCode(@Param("projectId") String projectId, @Param("moduleCode") String moduleCode);
	
	// ✅ 추가: 구성원 여부 확인 (권한 체크용)
	public int countProjectMember(@Param("projectId") String projectId, @Param("userId") String userId);
	
	// ✅ 추가: 관리자용 전체 프로젝트 조회 (검색+페이징)
	public List<ProjectVO> selectAllProjectsWithSearch(ProjectVO searchVO);
	public int selectAllProjectsCount(ProjectVO searchVO);
	
	// ✅ 추가: 프로젝트 상태 업데이트
	public int updateProjectStatus(@Param("projectId") String projectId, @Param("statusCode") String statusCode);
	
	void updateProjectGitUrl(@Param("projectId") String projectId, @Param("gitUrl") String gitUrl);
}
