package com.pixcel.app.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.workflow.service.WorkflowVO;

public interface WorkflowMapper {

	// 현재 사용자가 소유한 일감유형 목록을 조회한다.
	List<WorkflowVO> selectIssueTypeList(@Param("userId") String userId);

	// 현재 관리자가 생성한 역할 목록을 조회한다.
	List<WorkflowVO> selectRoleList(@Param("userId") String userId);

	// 현재 사용자가 소유한 일감상태 목록을 조회한다.
	List<WorkflowVO> selectIssueStatusList(WorkflowVO searchVO);

	// 선택한 일감유형 + 역할 + 전환기준에 저장된 상태전환 목록을 조회한다.
	List<String> selectSavedTransitionKeyList(WorkflowVO workflowVO);

	// 선택한 일감유형이 현재 사용자의 소유인지 확인한다.
	int countIssueTypeByUser(@Param("issueTypeId") String issueTypeId, @Param("userId") String userId);

	// 선택한 역할이 현재 관리자가 생성한 역할인지 확인한다.
	int countRole(@Param("roleId") String roleId, @Param("userId") String userId);

	// 선택한 상태들이 현재 사용자의 소유인지 확인한다.
	int countStatusByUser(@Param("userId") String userId, @Param("statusIdList") List<String> statusIdList);

	// 기존 상태전환 설정을 삭제한다.
	int deleteWorkflowTransition(WorkflowVO workflowVO);

	// 상태전환 설정을 등록한다.
	int insertWorkflowTransition(WorkflowVO workflowVO);
}