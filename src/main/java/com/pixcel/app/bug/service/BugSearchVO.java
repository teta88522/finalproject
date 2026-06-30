package com.pixcel.app.bug.service;

import lombok.Data;

@Data
public class BugSearchVO {
	private String projectId;
	
	private String projectName;
	private String bugStatusCode;
	private String assigneeId;
	private String severiyCode;
	private String priorityCode;
	private String reporterId;
	private String foundVersion;
	private String title;

}
