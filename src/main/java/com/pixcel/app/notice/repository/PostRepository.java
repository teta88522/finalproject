package com.pixcel.app.notice.repository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.notice.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String> {
	@Query(value = "SELECT * FROM (" +
            "  SELECT * FROM POST " + 
            "  WHERE post_id LIKE :prefix || '%' " +
            "  ORDER BY post_id DESC" +
            ") WHERE ROWNUM <= 1", 
    nativeQuery = true)
	Optional<PostEntity> findLatestPostIdByPrefix(@Param("prefix") String prefix);

	@Modifying
	@Transactional
	@Query("DELETE FROM PostEntity p WHERE p.postId = :postId")
	int deletePostByPostId(@Param("postId") String postId);

	long countByBoardId(String boardId); // 📝 실시간 글 카운팅 쿼리 메소드 추가
}
