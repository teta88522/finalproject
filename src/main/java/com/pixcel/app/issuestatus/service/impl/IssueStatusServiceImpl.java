package com.pixcel.app.issuestatus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.issuestatus.mapper.IssueStatusMapper;
import com.pixcel.app.issuestatus.service.IssueStatusService;
import com.pixcel.app.issuestatus.service.IssueStatusVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueStatusServiceImpl implements IssueStatusService {

    private final IssueStatusMapper issueStatusMapper;

    // 사용자별 일감 상태 전체 목록을 조회한다.
    @Override
    public List<IssueStatusVO> getIssueStatusList(String userId) {
        validateUserId(userId);

        return issueStatusMapper.selectIssueStatusList(userId);
    }

    // 사용자별 일감 상태 ID 기준으로 상세 정보를 조회한다.
    @Override
    public IssueStatusVO getIssueStatusDetail(String issueStatusId, String userId) {
        validateUserId(userId);

        IssueStatusVO issueStatus = issueStatusMapper.selectIssueStatusDetail(issueStatusId, userId);

        if (issueStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 일감 상태입니다.");
        }

        return issueStatus;
    }

    // 신규 일감 상태를 등록한다.
    @Override
    @Transactional
    public void createIssueStatus(IssueStatusVO issueStatus, String userId) {
        validateUserId(userId);

        issueStatus.setUserId(userId);

        validateIssueStatus(issueStatus);

        int duplicateCount = issueStatusMapper.countDuplicateStatusName(
                issueStatus.getUserId(),
                issueStatus.getStatusName(),
                null
        );

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 사용 중인 상태명입니다.");
        }

        issueStatusMapper.insertIssueStatus(issueStatus);
    }

    // 기존 일감 상태 정보를 수정한다.
    @Override
    @Transactional
    public void modifyIssueStatus(IssueStatusVO issueStatus, String userId) {
        validateUserId(userId);

        if (issueStatus.getIssueStatusId() == null || issueStatus.getIssueStatusId().trim().isEmpty()) {
            throw new IllegalArgumentException("일감 상태 ID가 없습니다.");
        }

        issueStatus.setUserId(userId);

        IssueStatusVO savedIssueStatus = issueStatusMapper.selectIssueStatusDetail(
                issueStatus.getIssueStatusId(),
                issueStatus.getUserId()
        );

        if (savedIssueStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 일감 상태입니다.");
        }

        validateIssueStatus(issueStatus);

        int duplicateCount = issueStatusMapper.countDuplicateStatusName(
                issueStatus.getUserId(),
                issueStatus.getStatusName(),
                issueStatus.getIssueStatusId()
        );

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 사용 중인 상태명입니다.");
        }

        issueStatusMapper.updateIssueStatus(issueStatus);
    }

    // 사용 중이지 않은 일감 상태를 삭제한다.
    @Override
    @Transactional
    public void removeIssueStatus(String issueStatusId, String userId) {
        validateUserId(userId);

        IssueStatusVO issueStatus = issueStatusMapper.selectIssueStatusDetail(issueStatusId, userId);

        if (issueStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 일감 상태입니다.");
        }

        int usedCount = issueStatusMapper.countUsedIssueStatus(issueStatusId);

        if (usedCount > 0) {
            throw new IllegalArgumentException("이미 일감, 일감유형 또는 업무흐름에서 사용 중인 상태이므로 삭제할 수 없습니다.");
        }

        issueStatusMapper.deleteIssueStatus(issueStatusId, userId);
    }

    // 로그인 사용자 ID 값을 검증한다.
    private void validateUserId(String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
    }

    // 일감 상태 등록/수정 값을 검증한다.
    private void validateIssueStatus(IssueStatusVO issueStatus) {

        if (issueStatus.getStatusName() == null || issueStatus.getStatusName().trim().isEmpty()) {
            throw new IllegalArgumentException("상태명을 입력하세요.");
        }

        if (issueStatus.getClosedYn() == null || issueStatus.getClosedYn().trim().isEmpty()) {
            issueStatus.setClosedYn("N");
        }

        if (!"Y".equals(issueStatus.getClosedYn()) && !"N".equals(issueStatus.getClosedYn())) {
            throw new IllegalArgumentException("종료 여부 값이 올바르지 않습니다.");
        }
    }
}