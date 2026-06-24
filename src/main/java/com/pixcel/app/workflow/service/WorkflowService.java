package com.pixcel.app.workflow.service;

import java.util.List;

public interface WorkflowService {

	// 현재 사용자가 소유한 일감유형 목록을 조회한다.
	List<WorkflowVO> getIssueTypeList(String userId);

	// 현재 관리자가 생성한 역할 목록을 조회한다.
	List<WorkflowVO> getRoleList(String userId);

	// 현재 사용자가 소유한 일감상태 목록을 조회한다.
	List<WorkflowVO> getIssueStatusList(WorkflowVO searchVO);

	// 선택한 조건에 저장된 상태전환 키 목록을 조회한다.
	List<String> getSavedTransitionKeyList(WorkflowVO workflowVO);

	// 상태전환 설정을 저장한다.
	void saveWorkflowTransition(WorkflowVO workflowVO, String userId);
}