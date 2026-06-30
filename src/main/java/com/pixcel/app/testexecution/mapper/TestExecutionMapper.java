package com.pixcel.app.testexecution.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.testexecution.service.TestExecutionVO;

@Mapper
public interface TestExecutionMapper {
	public TestExecutionVO selectTestExecutionInfo(@Param("projectId")String projectId, @Param("testId")String testId);
	
	public List<TestExecutionVO> selectExecutionCaseList(String testId);
	
	public int selectNextRetryNo(String mappingId);
	
	public int insertTestExecution(TestExecutionVO testExecutionVO);
	
	public int insertTestExecutionList(TestExecutionVO testExecutionVO);
}
