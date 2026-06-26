package com.pixcel.app.test.service;

import lombok.Data;

@Data
public class TestSearchVO {
	
	private String projectId;
	
	private String projectName;
	private String versionName;
	private String teamName;
	private String title;
	
	private String statusCode;
	private String testTypeCode;
	private String priorityCode;
	
	private String assigneeName;
	
	private String loginUserId;
	private String subscribeYn;
	
}
