package com.pixcel.app.workhistory.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.workhistory.service.WorkhistoryVO;

public interface WorkhistoryMapper {

	/* 프로젝트 접근 확인과 프로젝트 기본 정보 조회. */
	WorkhistoryVO selectProjectAccess(
			@Param("projectId") String projectId,
			@Param("userId") String userId);

	/* 작업내역 페이징용 ID 선조회. */
	List<WorkhistoryVO> selectWorkhistoryPageIds(WorkhistoryVO searchVO);

	/* 현재 페이지 작업내역 상세 row 조회. */
	List<WorkhistoryVO> selectWorkhistoryRowsByIds(
			@Param("projectId") String projectId,
			@Param("historyIdList") List<String> historyIdList);

	/* 선택된 필터 값의 표시명 조회. */
	List<WorkhistoryVO> selectSelectedFilterRows(WorkhistoryVO searchVO);

	/* 작업내역 필터 옵션 Ajax 조회. */
	List<WorkhistoryVO> selectFilterOptionRows(
			@Param("projectId") String projectId,
			@Param("userId") String userId);
}
