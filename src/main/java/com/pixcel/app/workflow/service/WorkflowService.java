package com.pixcel.app.workflow.service;

import java.util.List;

public interface WorkflowService {

	List<WorkflowVO> getIssueTypeList(String userId);

	List<WorkflowVO> getRoleList(String userId);

	List<WorkflowVO> getIssueStatusList(WorkflowVO searchVO);

	List<String> getSavedTransitionKeyList(WorkflowVO workflowVO);

	void saveWorkflowTransition(WorkflowVO workflowVO, String userId);

	// ==============================
	// 업무흐름 전체조회 추가
	// 역할 x 일감유형별 상태전환 설정 개수 조회
	// ==============================
	List<WorkflowVO> getWorkflowCountList(String userId);
	
	// ==============================
	// 업무흐름 복사 추가
	// 원본 업무흐름을 대상 조건으로 복사
	// ==============================
	void copyWorkflowTransition(WorkflowVO workflowVO, String userId);
}