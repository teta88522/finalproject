package com.pixcel.app.testcase.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

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

}
