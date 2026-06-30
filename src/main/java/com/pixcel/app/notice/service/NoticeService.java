package com.pixcel.app.notice.service;

import java.util.List;

public interface NoticeService {
	public String createNoticeBoard(NoticeRequestDTO requestDto);
	public List<NoticeRequestDTO> getBoardList(String projectId);
	public NoticeRequestDTO getBoardDetail(String boardId);
}
