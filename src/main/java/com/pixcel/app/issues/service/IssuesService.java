package com.pixcel.app.issues.service;

import java.util.List;
import java.util.Map;

public interface IssuesService {

	// ==============================
	// 프로젝트 공통
	// ==============================

	// 현재 사용자가 접근 가능한 프로젝트 목록을 조회한다.
	List<IssuesVO> getProjectList(String userId);

	// 요청 프로젝트가 있으면 접근 권한 검증 후 projectId를 반환한다.
	String getSelectedProjectId(String requestedProjectId, String userId);

	// 프로젝트 상세를 조회하고 접근 권한을 검증한다.
	IssuesVO getProjectDetail(String projectId, String userId);

	// ==============================
	// 일감 전체조회
	// ==============================

	// 프로젝트 기준 일감 목록을 조회한다.
	List<IssuesVO> getIssueList(String projectId, IssuesVO searchVO, String userId);

	Map<String, Object> getIssueListPageData(String projectId, IssuesVO searchVO, String userId);

	Map<String, Object> getIssueListFilterData(String projectId, String userId);

	Map<String, Object> getIssueReportPageData(String projectId, String userId);

	// 일감 상태 필터 목록을 조회한다.
	List<IssuesVO> getIssueStatusList(String projectId, String userId);

	// 일감 생성 권한 여부를 조회한다.
	boolean canCreateIssue(String projectId, String userId);

	// 마일스톤 생성 권한 여부를 조회한다.
	boolean canCreateMilestone(String projectId, String userId);

	// 일감 삭제 권한 여부를 조회한다.
	boolean canDeleteIssue(String projectId, String userId);

	Map<String, Object> getIssueDetailPageData(String projectId, String issueId, String userId);

	void validateIssueAccess(String projectId, String issueId, String userId);

	void updateIssue(IssuesVO issue, String userId);

	void recordIssueFileAddHistory(String issueId, String userId, int uploadCount);

	void deleteIssueFile(String projectId, String issueId, String fileId, String userId);

	int deleteIssueFiles(String projectId, String issueId, List<String> fileIds, String userId);

	// 일감 전체조회에서 선택한 일감을 삭제한다.
	void deleteIssue(String projectId, String issueId, String userId);

	// ==============================
	// 일감 생성
	// ==============================

	// 일감 생성 화면용 프로젝트 상세를 조회한다.
	IssuesVO getProjectDetailForCreate(String projectId, String userId);

	Map<String, Object> getIssueCreatePageData(String projectId, String userId);

	Map<String, Object> getIssueCreateExtraOptionData(String projectId, String userId);

	// 일감 생성 화면의 일감유형 목록을 조회한다.
	List<IssuesVO> getIssueTypeList(String projectId, String userId);

	// 일감유형별 표준 항목 설정 목록을 조회한다.
	List<IssuesVO> getFieldSettingList(String projectId, String userId);

	// 생성 가능한 버전 목록을 조회한다.
	List<IssuesVO> getVersionList(String projectId, String userId);

	// 생성 가능한 마일스톤 목록을 조회한다.
	List<IssuesVO> getMilestoneList(String projectId, String userId);

	// 일감 우선순위 코드값 목록을 조회한다.
	List<IssuesVO> getPriorityList(String projectId, String userId);

	// 프로젝트 담당자 후보 목록을 조회한다.
	List<IssuesVO> getAssigneeList(String projectId, String userId);

	// 상위일감 후보 목록을 조회한다.
	List<IssuesVO> getParentIssueList(String projectId, String userId);

	// 신규 일감을 등록한다.
	void createIssue(IssuesVO issue, String userId);
}
