package com.pixcel.app.test.service;

import java.util.List;

import lombok.Data;

@Data
public class TestVO {

	private String testId;
	private String versionId;
	private String title;
	private String description;
	
	private String projectId;
	private String projectName;
	private String versionName;
	
	private String priorityCode;
	private String priorityName;
	
	private String testTypeCode;
	private String testTypeName;
	
	private String statusCode;
	private String statusName;
	
	private String assigneeId;
	private String assigneeName;
	private String assigneeLoginId;
	
	private String createdBy;
	private String createdByName;
	private String createdByLoginId;
	
	private String startDate;
	private String endDate;
	private String createdAt;
	private String updatedAt;
	
	private String teamName;
	
	private int totalCaseCount;
	private int executionCount;
	private int successCount;
	private int failCount;
	private int holdCount;
	private int progressCount;
	
	private int progressRate;
	
	private List<String> testCaseId;
}
