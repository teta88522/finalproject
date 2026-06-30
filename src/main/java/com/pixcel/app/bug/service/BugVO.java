package com.pixcel.app.bug.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class BugVO {
		private String bugId;
		private String executionId;
		
		private String title;

		private String bugTypeCode;
		private String severityCode;
		private String priorityCode;
		private String bugStatusCode;
		
		private String foundVersion;
		private String testEnv;
		
		private String description;
		private String actualResult;
		private String reproduceStep;
		private String expectedResult;
		
		private String assigneeId;
		private String reporterId;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date occuredAt;
		
		private Date createAt;
		private Date updateAt;
		
		private String projectId;
		private String projectName;
		
		private String testId;
		private String testName;
		
		private String versionId;
		private String versionName;
		
		private String bugTypeName;
		private String severityName;
		private String priorityName;
		private String bugStatusName;
		
		private String assigneeName;
		private String reporterName;
		
		private String loginUserId;
		
		
}
