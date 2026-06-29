package com.pixcel.app.document.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DocumentVO {
	private String documentId;
	private String documentHistoryId;
	private String projectId;
	private String documentCategoryId;
	private String categoryName;
	private String settingCodeId;
	private String versionId;
	private String versionName;
	private String milestoneId;
	private String milestoneTitle;
	private String createdBy;
	private String userName;
	private String title;
	private String description;
	private String statusCode;
	private String statusName;
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;
	private int documentVersionId;
	private int nextVersion;
	private int totalCnt;
	
}
