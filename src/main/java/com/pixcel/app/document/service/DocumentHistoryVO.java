package com.pixcel.app.document.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class DocumentHistoryVO {
	private String documentHistoryId;
	private String documentId;
	private String createdBy;
	private String userName;
	private String title;
	private String description;
	private String statusCode;
	private String statusName;
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt; 
	private int documentVersionId;
}
