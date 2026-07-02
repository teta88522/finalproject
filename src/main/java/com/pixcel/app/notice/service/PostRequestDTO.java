package com.pixcel.app.notice.service;

import java.time.LocalDateTime;

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
public class PostRequestDTO {
	private String postId;
	private String boardId;
	private String title;
	private String content;
	private Integer viewCount;
	private String createdBy;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String userName;
	private String boardName; // 📝 추가: 게시판 이름
}
