package com.pixcel.app.issues.service;

import java.util.List;

public interface IssuesService {

	// 현재 사용자가 접근 가능한 프로젝트 목록을 조회한다.
	List<IssuesVO> getProjectList(String userId);

	// 요청 프로젝트가 없으면 첫 번째 접근 가능 프로젝트 ID를 반환한다.
	String getSelectedProjectId(String requestedProjectId, String userId);

	// ==============================
	// 일감 생성 URL 구조 수정
	// 프로젝트 상세를 조회하고 접근 권한을 검증한다.
	// ==============================
	IssuesVO getProjectDetailForCreate(String projectId, String userId);

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
