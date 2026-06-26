package com.pixcel.app.test.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.test.mapper.TestMapper;
import com.pixcel.app.test.service.TestGroupVO;
import com.pixcel.app.test.service.TestSearchVO;
import com.pixcel.app.test.service.TestService;
import com.pixcel.app.test.service.TestSummaryVO;
import com.pixcel.app.test.service.TestUserVO;
import com.pixcel.app.test.service.TestVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;

    @Override
    public TestSummaryVO selectAdminTestSummary(TestSearchVO testSearchVO) {

        TestSummaryVO testSummaryVO = testMapper.selectAdminTestSummary(testSearchVO);

        if (testSummaryVO == null) {
            testSummaryVO = new TestSummaryVO();
            testSummaryVO.setTotalCount(0);
            testSummaryVO.setExpectedCount(0);
            testSummaryVO.setProgressCount(0);
            testSummaryVO.setEndCount(0);
        }

        return testSummaryVO;
    }

    @Override
    public List<TestVO> selectAdminTestList(TestSearchVO testSearchVO) {
        return testMapper.selectAdminTestList(testSearchVO);
    }

    @Override
    public TestSummaryVO selectUserTestSummary(TestSearchVO testSearchVO) {

        TestSummaryVO testSummaryVO = testMapper.selectUserTestSummary(testSearchVO);

        if (testSummaryVO == null) {
            testSummaryVO = new TestSummaryVO();
            testSummaryVO.setTotalCount(0);
            testSummaryVO.setExpectedCount(0);
            testSummaryVO.setProgressCount(0);
            testSummaryVO.setEndCount(0);
        }

        return testSummaryVO;
    }

    @Override
    public List<TestVO> selectUserTestList(TestSearchVO testSearchVO) {
        return testMapper.selectUserTestList(testSearchVO);
    }

    @Override
    public List<TestVO> selectProjectVersionList(String projectId) {
        return testMapper.selectProjectVersionList(projectId);
    }

    @Override
    public List<TestUserVO> selectTestAssigneeList(String projectId) {
        return testMapper.selectTestAssigneeList(projectId);
    }

	@Override
	@Transactional
	public int insertTestWithCases(TestVO testVO) {
		
		  System.out.println("========== TEST INSERT DEBUG ==========");
		    System.out.println("testId = " + testVO.getTestId());
		    System.out.println("title = " + testVO.getTitle());
		    System.out.println("testCaseIds = " + testVO.getTestCaseId());
		    System.out.println("=======================================");
		
		int result = testMapper.insertTest(testVO);
		
		System.out.println("insert 후 testId = " + testVO.getTestId());
		if(testVO.getTestCaseId() != null && !testVO.getTestCaseId().isEmpty()) {
			testMapper.insertTestCaseMapping(testVO);
		}
		return result;
	}

	@Override
	public List<TestGroupVO> selectProjectGroupList(String projectId) {
		return testMapper.selectProjectGroupList(projectId);
	}
}