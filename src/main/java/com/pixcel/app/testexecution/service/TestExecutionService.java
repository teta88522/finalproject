package com.pixcel.app.testexecution.service;

import java.util.List;

public interface TestExecutionService {
	public TestExecutionVO selectTestExecutionInfo(String projectId, String testId);
	public List<TestExecutionVO> selectExecutionCaseList(String testId);
	public int insertTestExecution(TestExecutionVO testExecutionVO);
	public int insertTestExecutionList(TestExecutionVO testExecutionVO);
	public int selectCurrentRetryNo(String mappingId);
}
