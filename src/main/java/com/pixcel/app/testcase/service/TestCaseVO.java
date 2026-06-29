package com.pixcel.app.testcase.service;

import java.util.List;

import com.pixcel.app.file.service.FileVO;

import lombok.Data;

@Data
public class TestCaseVO {
	
	private String testCaseId;
	
	private String projectId;
	
	private String versionId;
	private String versionName;
	
	private String title;
	
	private String testTypeCode;
	private String testTypeName;
	
	private String statusCode;
	private String statusName;
	
	private String priorityCode;
	private String priorityName;
	
	private String preCondition;
	private String expectedResult;
	private String description;
	private String exeEnviroment;
	
	private String createdBy;
	private String createdByName;
	
	private String createdAt;
	private String updateAt;
	
	private List<TestCaseStepVO> stepList; 
	private List<String> stepDescriptionList;
	private List<String> stepExpectedResultList;
	private List<FileVO> fileList;
	
}
