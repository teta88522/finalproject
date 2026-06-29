package com.pixcel.app.testcase.service;

import java.util.List;

public interface TestCaseService {

	// 테스트 생성 화면용 테스트 케이스 총 개수
	public int selectTestCaseCount(TestCaseSearchVO testCaseSearchVO);
	
	// 테스트 생성 화면용 테스트 케이스 목록
	public List<TestCaseVO> selectTestCaseList(TestCaseSearchVO testCaseSearchVO);
	
	// 테스트 케이스 작성
	public int insertTestCaseWithSteps(TestCaseVO testCaseVO);
	
	public TestCaseVO selectTestCaseDetail(String projectId, String testCaseId);
	
	public int updateTestCaseWithSteps(TestCaseVO testCaseVO);
}
