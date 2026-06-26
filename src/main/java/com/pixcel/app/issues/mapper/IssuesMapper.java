package com.pixcel.app.issues.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.issues.service.IssuesVO;

public interface IssuesMapper {

	// ==============================
	// 프로젝트 공통
	// ==============================

	List<IssuesVO> selectProjectList(@Param("userId") String userId);

	IssuesVO selectProjectDetailForUser(@Param("projectId") String projectId, @Param("userId") String userId);

	int countProjectPermission(@Param("projectId") String projectId, @Param("userId") String userId,
			@Param("permissionCode") String permissionCode, @Param("permissionName") String permissionName);

	String selectProjectForUpdate(@Param("projectId") String projectId);

	Integer selectNextIssueNo(@Param("projectId") String projectId);

	// ==============================
	// 일감 전체조회
	// ==============================
	
	// 페이징
	int countIssueList(IssuesVO searchVO);

	List<IssuesVO> selectIssueList(IssuesVO searchVO);

	List<IssuesVO> selectIssueStatusList(@Param("projectId") String projectId);

	// ==============================
	// 일감 생성 / 조회 공통 선택 목록
	// ==============================

	List<IssuesVO> selectIssueTypeList(@Param("projectId") String projectId);

	IssuesVO selectIssueTypeDetailForCreate(@Param("projectId") String projectId,
			@Param("issueTypeId") String issueTypeId);

	List<IssuesVO> selectFieldSettingListByProject(@Param("projectId") String projectId);

	List<IssuesVO> selectFieldSettingListByIssueType(@Param("issueTypeId") String issueTypeId);

	List<IssuesVO> selectVersionList(@Param("projectId") String projectId);

	List<IssuesVO> selectMilestoneList(@Param("projectId") String projectId);

	List<IssuesVO> selectPriorityList(@Param("projectId") String projectId);

	List<IssuesVO> selectAssigneeList(@Param("projectId") String projectId);

	List<IssuesVO> selectParentIssueList(@Param("projectId") String projectId);

	// ==============================
	// 일감 생성 검증
	// ==============================

	int countVersionForCreate(@Param("projectId") String projectId, @Param("versionId") String versionId);

	int countPriorityForProject(@Param("projectId") String projectId, @Param("settingCodeId") String settingCodeId);

	int countMilestoneForCreate(@Param("projectId") String projectId, @Param("versionId") String versionId,
			@Param("milestoneId") String milestoneId);

	int countAssigneeForProject(@Param("projectId") String projectId, @Param("assigneeId") String assigneeId);

	int countParentIssueForProject(@Param("projectId") String projectId, @Param("parentIssueId") String parentIssueId);

	// ==============================
	// 일감 CUD
	// ==============================

	int insertIssue(IssuesVO issue);
	
	int countChildIssue(
	        @Param("projectId") String projectId,
	        @Param("issueId") String issueId
	);

	int deleteIssue(@Param("projectId") String projectId, @Param("issueId") String issueId);
}