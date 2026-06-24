package com.pixcel.app.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectVO;

@Mapper
public interface ProjectMapper {
	// 1. 프로젝트 마스터 정보 등록
	public int insertProject(ProjectVO projectVO);
	
	// 2. 프로젝트별 선택 모듈 다중 등록
	public int insertProjectModules(ProjectVO projectVO);
	
	// 자료실 관련
	public List<ProjectVO> selectAllProjects();
	
	//260623 고동현 프로젝트 리스트 관련추가
	//관리자
	public List<ProjectVO> selectMyCreatedProjectList(@Param("userId") String userId);
	//일반이용자
	public List<ProjectVO> selectMyJoinedProjectList(@Param("userId") String userId);
	
	// 프로젝트 상세 조회
	public ProjectVO selectProjectDetail(@Param("projectId") String projectId);
	
	//프로젝트 설정 - 구성원 목록 조회
	public List<ProjectMemberVO> selectProjectMemberList(@Param("projectId") String projectId);
	
	// 프로젝트 구성원 추가 후보 목록 조회
	public List<ProjectMemberVO> selectProjectMemberCandidateList(@Param("projectId") String projectId);

	// 프로젝트 역할 목록 조회
	public List<ProjectRoleVO> selectProjectRoleList(@Param("projectId") String projectId);

	// 프로젝트 구성원 중복 확인
	public int selectProjectMemberDuplicateCount(@Param("projectId") String projectId,
	                                             @Param("teamMemberId") String teamMemberId);

	// 프로젝트 구성원 ID 다음 번호
	public int selectProjectMemberNextNo();

	// 프로젝트 구성원 등록
	public int insertProjectMember(ProjectMemberVO projectMemberVO);
	
	// 프로젝트 구성원 단건 조회
	public ProjectMemberVO selectProjectMemberDetail(@Param("projectMemberId") String projectMemberId);

	// 프로젝트 구성원 역할 수정
	public int updateProjectMemberRole(ProjectMemberVO projectMemberVO);

	// 프로젝트 구성원 삭제
	public int deleteProjectMember(@Param("projectMemberId") String projectMemberId);
}
