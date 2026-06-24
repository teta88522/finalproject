package com.pixcel.app.issuetype.service;

import java.util.List;

public interface IssueTypeService {

	// 검색조건에 맞는 사용자별 일감유형 목록을 조회한다.
	List<IssueTypeVO> getIssueTypeList(IssueTypeVO searchVO);
	
	// 일감유형별 표준 항목 사용 여부 요약 목록을 조회한다.
	List<IssueTypeVO> getIssueTypeSummaryList(IssueTypeVO searchVO);

    // 사용자별 일감유형 상세 정보를 조회한다.
    IssueTypeVO getIssueTypeDetail(String issueTypeId, String userId);

    // 현재 사용자가 소유한 프로젝트 목록을 조회한다.
    List<IssueTypeVO> getProjectList(String userId);

    // 신규 일감유형을 등록한다.
    void createIssueType(IssueTypeVO issueType, String userId);

    // 기존 일감유형을 복사하여 신규 일감유형으로 등록한다.
    void copyIssueType(IssueTypeVO issueType, String userId);

    // 사용 중이지 않은 일감유형을 삭제한다.
    void removeIssueType(String issueTypeId, String userId);
}