package com.pixcel.app.notice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.notice.service.PostRequestDTO;
import com.pixcel.app.notice.service.PostSearchDTO;

public interface PostMapper {
	List<PostRequestDTO> selectPostList(String boardId, PostSearchDTO searchDTO, int start, int end);
    long countPostList(String boardId, PostSearchDTO searchDTO);
    int deletePostFile(@Param("postId") String postId, @Param("fileId") String fileId);

    List<PostRequestDTO> selectLatestPosts(@Param("projectId") String projectId);
    List<PostRequestDTO> selectPopularPosts(@Param("projectId") String projectId);
}
