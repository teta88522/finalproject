package com.pixcel.app.project.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.project.mapper.ProjectMapper;
import com.pixcel.app.project.service.IssueStatVO;
import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectModulesVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;

@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectMapper projectMapper;
 
	@Override
	@Transactional
	public int registerProject(ProjectVO projectVO) {
		int result = 0;
		result += projectMapper.insertProject(projectVO);
		if (projectVO.getModuleCodes() != null && projectVO.getModuleCodes().size() > 0) {
			result += projectMapper.insertProjectModules(projectVO);
		}
		return result;
	}
 
	@Override
	public List<ProjectVO> findAllProjects() {
		return projectMapper.selectAllProjects();
	}
 
	//260623 고동현 프로젝트 리스트 관련 추가
	// 관리자 - 검색+페이징
	@Override
	public List<ProjectVO> selectMyCreatedProjectList(ProjectVO projectVO) {
		projectVO.calcOffset();
		int total = projectMapper.selectMyCreatedProjectCount(projectVO);
		projectVO.setTotalCount(total);
		return projectMapper.selectMyCreatedProjectList(projectVO);
	}
 
	// 일반이용자 - 검색+페이징
	@Override
	public List<ProjectVO> selectMyJoinedProjectList(ProjectVO projectVO) {
		projectVO.calcOffset();
		int total = projectMapper.selectMyJoinedProjectCount(projectVO);
		projectVO.setTotalCount(total);
		return projectMapper.selectMyJoinedProjectList(projectVO);
	}
 
	@Override
	public ProjectVO selectProjectDetail(String projectId) {
		return projectMapper.selectProjectDetail(projectId);
	}
 
	@Override
	public List<ProjectMemberVO> selectProjectMemberList(String projectId) {
		return projectMapper.selectProjectMemberList(projectId);
	}
 
	@Override
	public List<ProjectMemberVO> selectProjectMemberCandidateList(String projectId) {
		return projectMapper.selectProjectMemberCandidateList(projectId);
	}
 
	@Override
	public List<ProjectRoleVO> selectProjectRoleList(String projectId) {
		return projectMapper.selectProjectRoleList(projectId);
	}
 
	@Override
	@Transactional
	public Map<String, Object> insertProjectMember(ProjectMemberVO projectMemberVO) {
		Map<String, Object> resultMap = new HashMap<>();
		int duplicateCount = projectMapper.selectProjectMemberDuplicateCount(
				projectMemberVO.getProjectId(), projectMemberVO.getTeamMemberId());
		if (duplicateCount > 0) {
			resultMap.put("result", false);
			resultMap.put("message", "이미 참가중인 구성원 입니다.");
			return resultMap;
		}
		int nextNo = projectMapper.selectProjectMemberNextNo();
		String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
		String projectMemberId = "PROJECT_MEMBER_" + yearMonth + "_" + String.format("%04d", nextNo);
		projectMemberVO.setProjectMemberId(projectMemberId);
		int insertResult = projectMapper.insertProjectMember(projectMemberVO);
		if (insertResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message", "구성원 등록에 실패했습니다.");
			return resultMap;
		}
		resultMap.put("result", true);
		resultMap.put("message", "구성원이 등록되었습니다.");
		resultMap.put("projectMemberId", projectMemberId);
		return resultMap;
	}
 
	@Override
	public ProjectMemberVO selectProjectMemberDetail(String projectMemberId) {
		return projectMapper.selectProjectMemberDetail(projectMemberId);
	}
 
	@Override
	@Transactional
	public Map<String, Object> updateProjectMemberRole(ProjectMemberVO projectMemberVO) {
		Map<String, Object> resultMap = new HashMap<>();
		int updateResult = projectMapper.updateProjectMemberRole(projectMemberVO);
		if (updateResult <= 0) {
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
	public Map<String, Object> deleteProjectMember(String projectMemberId) {
		Map<String, Object> resultMap = new HashMap<>();
		int deleteResult = projectMapper.deleteProjectMember(projectMemberId);
		if (deleteResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message", "구성원 삭제에 실패했습니다.");
			return resultMap;
		}
		resultMap.put("result", true);
		resultMap.put("message", "구성원 삭제가 성공하였습니다.");
		return resultMap;
	}
 
	@Override
	public List<ProjectModulesVO> selectAllModuleProjects() {
		return null;
	}
	
	@Override
	@Transactional
	public Map<String, Object> deleteProject(String projectId){
		Map<String, Object> resultMap = new HashMap<>();
		
		// 프로젝트 모듈 삭제
		projectMapper.deleteProjectModulesByProjectId(projectId);
		
		// 프로젝트 멤버 삭제
		projectMapper.deleteProjectMembersByProjectId(projectId);
		
		// 프로젝트 삭제
		int deleteResult = projectMapper.deleteProject(projectId);
		if(deleteResult <= 0) {
			resultMap.put("result", false);
			resultMap.put("message", "프로젝트 삭제에 실패했습니다.");
			return resultMap;
		}
		resultMap.put("result", true);
		return resultMap;
	}

	@Override
	public List<IssueStatVO> selectIssueStatByProjectId(String projectId) {
		return projectMapper.selectIssueStatByProjectId(projectId);
	}
}
