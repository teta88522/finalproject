package com.pixcel.app.testexecution.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.testexecution.mapper.TestExecutionMapper;
import com.pixcel.app.testexecution.service.TestExecutionService;
import com.pixcel.app.testexecution.service.TestExecutionVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestExecutionServiceImpl implements TestExecutionService {

	private final TestExecutionMapper testExecutionMapper;
	
	@Override
	public TestExecutionVO selectTestExecutionInfo(String projectId, String testId) {
		return testExecutionMapper.selectTestExecutionInfo(projectId, testId);
	}

	@Override
	public List<TestExecutionVO> selectExecutionCaseList(String testId) {
		return testExecutionMapper.selectExecutionCaseList(testId);
	}

	@Override
	@Transactional
	public int insertTestExecution(TestExecutionVO testExecutionVO) {
		
		String executionId = testExecutionMapper.selectExecutionIdByMappingId(testExecutionVO.getMappingId());
		
		if(executionId == null || executionId.equals("")) {
			return testExecutionMapper.insertTestExecution(testExecutionVO);
		}
		
		testExecutionVO.setExecutionId(executionId);
		
		return testExecutionMapper.updateTestExecution(testExecutionVO);
	}

	@Override
	@Transactional
	public int insertTestExecutionList(TestExecutionVO testExecutionVO) {
	    if (testExecutionVO.getMappingIdList() == null || testExecutionVO.getMappingIdList().isEmpty()) {
	        return 0;
	    }

	    return testExecutionMapper.insertTestExecutionList(testExecutionVO);
	}
	
	

}
