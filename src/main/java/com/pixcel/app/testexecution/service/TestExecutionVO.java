package com.pixcel.app.testexecution.service;

import java.util.List;

import lombok.Data;

@Data
public class TestExecutionVO {
	private String projectId;
	private String testId;
	private String testTitle;
	private String versionId;
	private String versionName;
	private String teamName;
	
	private String statusCode;
	private String statusName;
	private String priorityCode;
	private String priorityName;
	private String testTypeCode;
	private String testTypeName;
	
	private String assigneeId;
	private String assigneeName;
	private String createBy;
	private String createdByName;
	
	private String description;
	private String startDate;
	private String endDate;
	private String createAt;
	private String updateAt;
	
	private String mappingId;
	private int sortOrder;
	
	private String testCaseId;
	private String testCaseTitle;
	private String testCaseTypeCode;
	private String testCaseTypeName;
	private String testCasePriorityCode;
	private String testCasePriorityName;
	private String preCondition;
	private String expectedResult;
	
	private String executionId;
	private String executorId;
	private String executorName;
	private String resultStatusCode;
	private String resultStatusName;
	private String executionComment;
	private int retryNo;
	private int nextRetryNo;
	private String executedAt;
	
	private List<String> mappingIdList;
	private List<String> resultStatusCodeList;
	private List<String> executionCommentList;
}
