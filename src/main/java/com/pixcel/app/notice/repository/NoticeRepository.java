package com.pixcel.app.notice.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pixcel.app.notice.entity.NoticeEntity;
import com.pixcel.app.notice.service.NoticeRequestDTO;

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
   public List<NoticeEntity> findByProjectId(String projectId);
}