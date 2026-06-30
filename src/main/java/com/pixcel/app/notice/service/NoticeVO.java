package com.pixcel.app.notice.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeVO {
	private String boardId;
	private String projectId;
	private String boardName;
	private LocalDate createdAt;
	private String createdBy;
	private String description;
}
