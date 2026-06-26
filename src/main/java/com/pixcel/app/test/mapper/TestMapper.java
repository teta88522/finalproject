package com.pixcel.app.test.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.test.service.TestGroupVO;
import com.pixcel.app.test.service.TestSearchVO;
import com.pixcel.app.test.service.TestSummaryVO;
import com.pixcel.app.test.service.TestUserVO;
import com.pixcel.app.test.service.TestVO;

@Mapper
public interface TestMapper {
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

    // 테스트 등록
    public int insertTest(TestVO testVO);
    
    // 테스트 매핑 테이블 등록
    public int insertTestCaseMapping(TestVO testVO);
    
    public List<TestGroupVO> selectProjectGroupList(String projectId);
}
