package com.pixcel.app.bug.service;

import java.util.List;

import lombok.Data;

@Data
public class BugSearchVO {
	private String projectId;
	
	private List<String> bugStatusCodeList;
	private List<String> severityCodeList;
	private List<String> priorityCodeList;
	
	private String assigneeId;
	private String reporterId;
	private String foundVersion;
	private String title;

}
