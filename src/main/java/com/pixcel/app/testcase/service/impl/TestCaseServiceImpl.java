package com.pixcel.app.testcase.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.testcase.mapper.TestCaseMapper;
import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseService;
import com.pixcel.app.testcase.service.TestCaseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

	private final TestCaseMapper testCaseMapper;
	
	@Override
	public int selectTestCaseCount(TestCaseSearchVO testCaseSearchVO) {
		return testCaseMapper.selectTestCaseCount(testCaseSearchVO);
	}

	@Override
	public List<TestCaseVO> selectTestCaseList(TestCaseSearchVO testCaseSearchVO) {
		return testCaseMapper.selectTestCaseList(testCaseSearchVO);
	}

	@Override
	@Transactional
	public int insertTestCaseWithSteps(TestCaseVO testCaseVO) {
		int result = testCaseMapper.insertTestCase(testCaseVO);
		
		if(testCaseVO.getStepDescriptionList() != null && !testCaseVO.getStepExpectedResultList().isEmpty()) {
			testCaseMapper.insertTestCaseStep(testCaseVO);
		}
		return result;
	}

	@Override
	public TestCaseVO selectTestCaseDetail(String projectId, String testCaseId) {
		TestCaseVO testCaseVO = testCaseMapper.selectTestCaseDetail(projectId, testCaseId);
		if(testCaseVO != null) {
			testCaseVO.setStepList(testCaseMapper.selectTestCaseStepList(testCaseId));
		}
		return testCaseVO;
	}

	@Override
	@Transactional
	public int updateTestCaseWithSteps(TestCaseVO testCaseVO) {
		
	    int result = testCaseMapper.updateTestCase(testCaseVO);

	    testCaseMapper.deleteTestCaseStep(testCaseVO.getTestCaseId());

	    if (testCaseVO.getStepDescriptionList() != null
	            && !testCaseVO.getStepDescriptionList().isEmpty()) {
	        testCaseMapper.insertTestCaseStep(testCaseVO);
	    }

	    return result;
	}

}
