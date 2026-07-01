package com.pixcel.app.notice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
	Page<PostRequestDTO> getPostListByBoardId(String boardId, PostSearchDTO searchDTO, Pageable pageable);
	public void createPost(PostRequestDTO dto);
	PostRequestDTO getPostDetail(String postId);
	public void updatePost(PostRequestDTO dto, List<String> deleteFileIds);
	public void deletePost(String postId);
	public List<PostRequestDTO> getLatestPosts(String projectId);
	public List<PostRequestDTO> getPopularPosts(String projectId);
}
