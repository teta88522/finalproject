package com.pixcel.app.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.workflow.service.WorkflowVO;

public interface WorkflowMapper {

	List<WorkflowVO> selectIssueTypeList(@Param("userId") String userId);

	List<WorkflowVO> selectRoleList(@Param("userId") String userId);

	List<WorkflowVO> selectIssueStatusList(WorkflowVO searchVO);

	List<String> selectSavedTransitionKeyList(WorkflowVO workflowVO);

	int countIssueTypeByUser(@Param("issueTypeId") String issueTypeId, @Param("userId") String userId);

	int countRole(@Param("roleId") String roleId, @Param("userId") String userId);

	int countStatusByUser(@Param("userId") String userId, @Param("statusIdList") List<String> statusIdList);

	int deleteWorkflowTransition(WorkflowVO workflowVO);

	int insertWorkflowTransition(WorkflowVO workflowVO);

	// ==============================
	// 업무흐름 전체조회 추가
	// 역할 x 일감유형별 상태전환 설정 개수 조회
	// ==============================
	List<WorkflowVO> selectWorkflowCountList(@Param("userId") String userId);
	
	// ==============================
	// 업무흐름 복사 추가
	// 원본 업무흐름 개수 확인
	// ==============================
	int countSourceWorkflowTransition(WorkflowVO workflowVO);

	// ==============================
	// 업무흐름 복사 추가
	// 대상 업무흐름 삭제
	// ==============================
	int deleteTargetWorkflowTransitionForCopy(WorkflowVO workflowVO);

	// ==============================
	// 업무흐름 복사 추가
	// 원본 업무흐름을 대상 조건으로 복사
	// ==============================
	int copyWorkflowTransition(WorkflowVO workflowVO);
}