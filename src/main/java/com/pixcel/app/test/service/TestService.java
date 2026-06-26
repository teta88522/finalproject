package com.pixcel.app.test.service;

import java.util.List;

public interface TestService {
	
	// 관리자 테스트 요약
	public TestSummaryVO selectAdminTestSummary(TestSearchVO testSearchVO);
	// 관리자 테스트 목록
	public List<TestVO> selectAdminTestList(TestSearchVO testSearchVO);
	// 사용자 테스트 요약
	public TestSummaryVO selectUserTestSummary(TestSearchVO testSearchVO);
	// 사용자 테스트 목록
	public List<TestVO> selectUserTestList(TestSearchVO testSearchVO);
	
	// 프로젝트 버전 목록
    public List<TestVO> selectProjectVersionList(String projectId);

    // 테스트 담당자 목록
    public List<TestUserVO> selectTestAssigneeList(String projectId);

    // 테스트 생성 + 테스트 케이스 매핑 테이블 등록
    public int insertTestWithCases(TestVO testVO);
    
    public List<TestGroupVO> selectProjectGroupList(String projectId); 
    
    

}
