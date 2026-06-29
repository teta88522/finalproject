package com.pixcel.app.testcase.service;

import lombok.Data;

@Data
public class TestCaseStepVO {
	
	private String stepId;
	private String testCaseId;
	
	private int stepNo;
	
	private String stepDescription;
	private String expectedResult;
}
