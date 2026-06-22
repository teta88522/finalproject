package com.pixcel.app.issuestatus.service;

import java.util.List;

public interface IssueStatusService {

    // 사용자별 일감 상태 전체 목록을 조회한다.
    List<IssueStatusVO> getIssueStatusList(String userId);

    // 사용자별 일감 상태 ID 기준으로 상세 정보를 조회한다.
    IssueStatusVO getIssueStatusDetail(String issueStatusId, String userId);

    // 신규 일감 상태를 등록한다.
    void createIssueStatus(IssueStatusVO issueStatus, String userId);

    // 기존 일감 상태 정보를 수정한다.
    void modifyIssueStatus(IssueStatusVO issueStatus, String userId);

    // 사용 중이지 않은 일감 상태를 삭제한다.
    void removeIssueStatus(String issueStatusId, String userId);
}