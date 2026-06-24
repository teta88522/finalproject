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
import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectRoleVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;

@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectMapper projectMapper;
	
	@Override
	@Transactional	// 마스터 등록과 모듈 등록 중 하나라도 실패한다면 통째로 롤백되도록 함
	public int registerProject(ProjectVO projectVO) {
		
		int result = 0;
		
		// 1. PROJECT 테이블에 기본 프로젝트 정보 등록
		result += projectMapper.insertProject(projectVO);
		
		// 2. 화면에서 사용자가 선택한 모듈 코드 리스트가 존재할 때만 다중 등록 실행
		if(projectVO.getModuleCodes() != null && projectVO.getModuleCodes().size() > 0) {
			result += projectMapper.insertProjectModules(projectVO);
		}
		
		return result;
	}
	
	// 자료실에서 사용할 프로젝트 목록을 불러오기 위한 기능 구현
	@Override
	public List<ProjectVO> findAllProjects(){
		return projectMapper.selectAllProjects();
	}

	//260623 고동현 프로젝트 리스트 관련 추가
	//관리자
	@Override
	public List<ProjectVO> selectMyCreatedProjectList(String userId) {
		
		return projectMapper.selectMyCreatedProjectList(userId);
	}
	//일반이용자
	@Override
	public List<ProjectVO> selectMyJoinedProjectList(String userId) {
		return projectMapper.selectMyJoinedProjectList(userId);
	}

	//상세화면 껍데기
	@Override
	public ProjectVO selectProjectDetail(String projectId) {
		return projectMapper.selectProjectDetail(projectId);
	}

	//프로젝트 설정 - 구성원 목록조회
	@Override
	public List<ProjectMemberVO> selectProjectMemberList(String projectId) {
		return projectMapper.selectProjectMemberList(projectId);
	}

	
	//프로젝트 구성원 후보 목록 조회
	@Override
	public List<ProjectMemberVO> selectProjectMemberCandidateList(String projectId) {
		return projectMapper.selectProjectMemberCandidateList(projectId);
	}

	//프로젝트 롤 조회
	@Override
	public List<ProjectRoleVO> selectProjectRoleList(String projectId) {
		return projectMapper.selectProjectRoleList(projectId);
	}

	//프로젝트 구성원 등록
	@Override
	@Transactional
	public Map<String, Object> insertProjectMember(ProjectMemberVO projectMemberVO) {
		
		Map<String,Object> resultMap = new HashMap<>();
		
		int duplicateCount = projectMapper.selectProjectMemberDuplicateCount(
				projectMemberVO.getProjectId(),
				projectMemberVO.getTeamMemberId());
		
		if(duplicateCount > 0 ) {
			resultMap.put("result", false);
			resultMap.put("message","이미 참가중인 구성원 입니다.");
			return resultMap;
		}
		
		int nextNo = projectMapper.selectProjectMemberNextNo();
		
	    String yearMonth = LocalDate.now()
	            .format(DateTimeFormatter.ofPattern("yyyyMM"));
	    
	    String projectMemberId =
	            "PROJECT_MEMBER_"
	            + yearMonth
	            + "_"
	            + String.format("%04d", nextNo);
	    
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

	//프로젝트 멤버 단건 조회
	@Override
	public ProjectMemberVO selectProjectMemberDetail(String projectMemberId) {
		return projectMapper.selectProjectMemberDetail(projectMemberId);
	}

	//프로젝트 멤버 구성원 역할 수정
	@Override
	@Transactional
	public Map<String, Object> updateProjectMemberRole(ProjectMemberVO projectMemberVO) {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		int updateResult = projectMapper.updateProjectMemberRole(projectMemberVO);
		
		if(updateResult <= 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "구성원 역할 수정에 실패했습니다.");
			return resultMap;
		}
		
		resultMap.put("result", true);
		resultMap.put("message", "구성원 역할이 수정되었습니다.");
		
		return resultMap;
	}
	
	//프로젝트 멤버 구성원 삭제
	@Override
	@Transactional
	public Map<String, Object> deleteProjectMember(String projectMemberId) {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		int deleteResult = projectMapper.deleteProjectMember(projectMemberId);
		
		if(deleteResult <= 0 ) {
			resultMap.put("result", false);
			resultMap.put("message", "구성원 삭제에 실패했습니다.");
			return resultMap;
		}
		
		resultMap.put("result", true);
		resultMap.put("message", "구성원 삭제가 성공하였습니다.");
		
		return resultMap;
	}

}
