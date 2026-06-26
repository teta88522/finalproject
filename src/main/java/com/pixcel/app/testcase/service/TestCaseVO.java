package com.pixcel.app.testcase.service;

import lombok.Data;

@Data
public class TestCaseVO {
	
	private String testCaseId;
	
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
	
	
}
