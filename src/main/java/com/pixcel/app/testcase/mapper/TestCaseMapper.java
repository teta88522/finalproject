package com.pixcel.app.testcase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseStepVO;
import com.pixcel.app.testcase.service.TestCaseVO;

@Mapper
public interface TestCaseMapper {
    // 테스트 생성 화면용 테스트케이스 총 개수
    public int selectTestCaseCount(TestCaseSearchVO testCaseSearchVO);

    // 테스트 생성 화면용 테스트케이스 목록
    public List<TestCaseVO> selectTestCaseList(TestCaseSearchVO testCaseSearchVO);
    
    // 테스트 케이스 등록
    public int insertTestCase(TestCaseVO testCaseVO);
    
    // 테스트 케이스 절차 등록
    public int insertTestCaseStep(TestCaseVO testCaseVO);
    
    public TestCaseVO selectTestCaseDetail(@Param("projectId") String projectId,
    									   @Param("testCaseId") String testCaseId);
    
    public List<TestCaseStepVO> selectTestCaseStepList(String testCaseId);
    
    public int updateTestCase(TestCaseVO testCaseVO);

    public int deleteTestCaseStep(String testCaseId);
    
    //권한체크
    public int checkTestCaseOwner(TestCaseVO testCaseVO);
}
