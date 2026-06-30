package com.pixcel.app.timelog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.timelog.service.TimelogVO;

public interface TimelogMapper {

	List<TimelogVO> selectTimelogFormRows(
			@Param("projectId") String projectId,
			@Param("userId") String userId,
			@Param("updatePermissionCode") String updatePermissionCode,
			@Param("updateOwnPermissionCode") String updateOwnPermissionCode,
			@Param("issueSearchKeyword") String issueSearchKeyword,
			@Param("selectedIssueId") String selectedIssueId);

	TimelogVO selectTimelogSaveValidation(
			@Param("projectId") String projectId,
			@Param("issueId") String issueId,
			@Param("settingCodeId") String settingCodeId,
			@Param("userId") String userId,
			@Param("updatePermissionCode") String updatePermissionCode,
			@Param("updateOwnPermissionCode") String updateOwnPermissionCode);

	int insertTimelog(TimelogVO timelog);

	TimelogVO selectTimelogDetail(
			@Param("projectId") String projectId,
			@Param("issueId") String issueId,
			@Param("timeLogId") String timeLogId,
			@Param("userId") String userId,
			@Param("updatePermissionCode") String updatePermissionCode,
			@Param("updateOwnPermissionCode") String updateOwnPermissionCode);

	int updateTimelog(TimelogVO timelog);

	int deleteTimelog(@Param("timeLogId") String timeLogId);

	List<TimelogVO> selectIssueTimelogSummaryRows(
			@Param("projectId") String projectId,
			@Param("issueId") String issueId);
}
