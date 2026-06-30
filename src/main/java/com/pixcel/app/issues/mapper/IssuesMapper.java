package com.pixcel.app.issues.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.issues.service.IssuesVO;

public interface IssuesMapper {

	// ==============================
	// 프로젝트 공통
	// ==============================

	List<IssuesVO> selectProjectList(@Param("userId") String userId);

	IssuesVO selectProjectDetailForUser(@Param("projectId") String projectId, @Param("userId") String userId);

	IssuesVO selectIssueCreateProjectAccess(@Param("projectId") String projectId, @Param("userId") String userId,
			@Param("permissionCode") String permissionCode);

	IssuesVO selectIssueListProjectAccess(@Param("projectId") String projectId, @Param("userId") String userId,
			@Param("issueCreatePermissionCode") String issueCreatePermissionCode,
			@Param("milestoneCreatePermissionCode") String milestoneCreatePermissionCode);

	int countProjectPermission(@Param("projectId") String projectId, @Param("userId") String userId,
			@Param("permissionCode") String permissionCode, @Param("permissionName") String permissionName);

	String selectProjectForUpdate(@Param("projectId") String projectId);

	Integer selectNextIssueNo(@Param("projectId") String projectId);

	Integer selectNextIssueNoForUpdate(@Param("projectId") String projectId);

	// ==============================
	// 일감 전체조회
	// ==============================

	List<IssuesVO> selectIssueList(IssuesVO searchVO);

	List<IssuesVO> selectIssueStatusList(@Param("projectId") String projectId);

	IssuesVO selectIssueDetail(@Param("projectId") String projectId, @Param("issueId") String issueId,
			@Param("userId") String userId, @Param("updatePermissionCode") String updatePermissionCode,
			@Param("updateOwnPermissionCode") String updateOwnPermissionCode,
			@Param("deletePermissionCode") String deletePermissionCode);

	int countIssueDetailAccess(@Param("projectId") String projectId, @Param("issueId") String issueId,
			@Param("userId") String userId);

	List<IssuesVO> selectIssueDetailRows(@Param("projectId") String projectId,
			@Param("issueId") String issueId, @Param("userId") String userId);

	List<IssuesVO> selectIssueHistoryRows(@Param("issueId") String issueId);

	int countIssueHistory(@Param("issueId") String issueId);

	List<IssuesVO> selectAvailableStatusList(@Param("projectId") String projectId,
			@Param("issueId") String issueId, @Param("userId") String userId);

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

	List<IssuesVO> selectIssueCreateBaseOptionRows(@Param("projectId") String projectId);

	List<IssuesVO> selectIssueCreateExtraOptionRows(@Param("projectId") String projectId);

	List<IssuesVO> selectIssueListOptionRows(@Param("projectId") String projectId);

	List<IssuesVO> selectIssueListSelectedOptionRows(IssuesVO searchVO);

	List<IssuesVO> selectIssueReportSourceRows(@Param("projectId") String projectId);

	IssuesVO selectIssueCreateSaveValidation(@Param("issue") IssuesVO issue, @Param("userId") String userId,
			@Param("permissionCode") String permissionCode);

	IssuesVO selectIssueUpdateSaveValidation(@Param("issue") IssuesVO issue, @Param("userId") String userId);

	// ==============================
	// 일감 CUD
	// ==============================

	int insertIssue(IssuesVO issue);

	IssuesVO selectIssueForUpdate(@Param("projectId") String projectId, @Param("issueId") String issueId);

	int countIssueUpdatePermission(@Param("projectId") String projectId, @Param("issueId") String issueId,
			@Param("userId") String userId, @Param("updatePermissionCode") String updatePermissionCode,
			@Param("updateOwnPermissionCode") String updateOwnPermissionCode);

	int updateIssue(IssuesVO issue);

	int insertIssueHistoryByProcedure(IssuesVO history);

	FileVO selectIssueFile(@Param("issueId") String issueId, @Param("fileId") String fileId);

	List<FileVO> selectIssueFileList(@Param("issueId") String issueId);

	int deleteIssueFile(@Param("issueId") String issueId, @Param("fileId") String fileId);

	int deleteIssueFilesByIssue(@Param("issueId") String issueId);

	int deleteIssueTimelogs(@Param("issueId") String issueId);
	
	int countChildIssue(
	        @Param("projectId") String projectId,
	        @Param("issueId") String issueId
	);

	int deleteIssue(@Param("projectId") String projectId, @Param("issueId") String issueId);
}
