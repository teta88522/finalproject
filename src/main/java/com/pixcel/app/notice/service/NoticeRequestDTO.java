package com.pixcel.app.notice.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequestDTO {
	private String boardId;
	private String projectId;
	private String boardName;
	private String createdBy;
	private String description;
	private String userName;
}
