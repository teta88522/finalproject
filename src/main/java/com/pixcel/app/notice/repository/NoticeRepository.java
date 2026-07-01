package com.pixcel.app.notice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pixcel.app.notice.entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, String> {

    // JPA의 자동 생성(findTopBy...) 대신 네이티브 쿼리로 ROWNUM 적용
    @Query(value = "SELECT * FROM (" +
                   "  SELECT * FROM board " +
                   "  WHERE board_id LIKE :prefix || '%' " +
                   "  ORDER BY board_id DESC" +
                   ") WHERE ROWNUM <= 1", 
           nativeQuery = true)
    Optional<NoticeEntity> findLatestBoardIdByPrefix(@Param("prefix") String prefix);

    @Query("SELECT n FROM NoticeEntity n LEFT JOIN FETCH n.user WHERE n.projectId = :projectId")
    List<NoticeEntity> findByProjectId(@Param("projectId") String projectId);
}