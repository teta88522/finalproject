package com.pixcel.app.issues.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.issues.mapper.IssuesMapper;
import com.pixcel.app.issues.service.IssuesService;
import com.pixcel.app.issues.service.IssuesVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssuesServiceImpl implements IssuesService {

    private final IssuesMapper issuesMapper;

    private static final String FIELD_ASSIGNEE = "ASSIGNEE";
    private static final String FIELD_MILESTONE = "MILESTONE";
    private static final String FIELD_PARENT_ISSUE = "PARENT_ISSUE";
    private static final String FIELD_START_DATE = "START_DATE";
    private static final String FIELD_DUE_DATE = "DUE_DATE";
    private static final String FIELD_ESTIMATED_HOURS = "ESTIMATED_HOURS";

    // 현재 사용자가 접근 가능한 프로젝트 목록을 조회한다.
    @Override
    public List<IssuesVO> getProjectList(String userId) {
        validateUserId(userId);
        return issuesMapper.selectProjectList(userId);
    }

    // 요청 프로젝트가 없으면 첫 번째 접근 가능 프로젝트 ID를 반환한다.
    @Override
    public String getSelectedProjectId(String requestedProjectId, String userId) {
        validateUserId(userId);

        if (isBlank(requestedProjectId)) {
            return null;
        }

        validateProjectAccess(requestedProjectId, userId);
        return requestedProjectId;
    }

    // 일감 생성 화면의 일감유형 목록을 조회한다.
    @Override
    public List<IssuesVO> getIssueTypeList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectIssueTypeList(projectId);
    }

    // 일감유형별 표준 항목 설정 목록을 조회한다.
    @Override
    public List<IssuesVO> getFieldSettingList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectFieldSettingListByProject(projectId);
    }

    // 생성 가능한 버전 목록을 조회한다.
    @Override
    public List<IssuesVO> getVersionList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectVersionList(projectId);
    }

    // 생성 가능한 마일스톤 목록을 조회한다.
    @Override
    public List<IssuesVO> getMilestoneList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectMilestoneList(projectId);
    }

    // 일감 우선순위 코드값 목록을 조회한다.
    @Override
    public List<IssuesVO> getPriorityList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectPriorityList(projectId);
    }

    // 프로젝트 담당자 후보 목록을 조회한다.
    @Override
    public List<IssuesVO> getAssigneeList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectAssigneeList(projectId);
    }

    // 상위일감 후보 목록을 조회한다.
    @Override
    public List<IssuesVO> getParentIssueList(String projectId, String userId) {
        validateProjectAccess(projectId, userId);
        return issuesMapper.selectParentIssueList(projectId);
    }

    // 신규 일감을 등록한다.
    @Override
    @Transactional
    public void createIssue(IssuesVO issue, String userId) {
        validateUserId(userId);
        validateBasicIssue(issue);
        validateProjectAccess(issue.getProjectId(), userId);
        validateCreatePermission(issue.getProjectId(), userId);

        IssuesVO issueType = issuesMapper.selectIssueTypeDetailForCreate(issue.getProjectId(), issue.getIssueTypeId());

        if (issueType == null) {
            throw new IllegalArgumentException("선택한 프로젝트에서 사용할 수 없는 일감 유형입니다.");
        }

        validateVersion(issue.getProjectId(), issue.getVersionId());
        validatePriority(issue.getProjectId(), issue.getSettingCodeId());

        issue.setIssueStatusId(issueType.getInitialStatusId());
        issue.setAuthorId(userId);
        issue.setTitle(issue.getTitle().trim());
        issue.setDescription(trimToNull(issue.getDescription()));
        issue.setAssigneeId(trimToNull(issue.getAssigneeId()));
        issue.setMilestoneId(trimToNull(issue.getMilestoneId()));
        issue.setParentIssueId(trimToNull(issue.getParentIssueId()));

        validateLength(issue);
        validateProgressRate(issue);
        validateDynamicFields(issue);

        issuesMapper.insertIssue(issue);
    }

    // 프로젝트 접근 가능 여부를 검증한다.
    private IssuesVO validateProjectAccess(String projectId, String userId) {
        validateUserId(userId);

        if (isBlank(projectId)) {
            throw new IllegalArgumentException("프로젝트를 선택해주세요.");
        }

        IssuesVO project = issuesMapper.selectProjectDetailForUser(projectId, userId);

        if (project == null) {
            throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
        }

        return project;
    }

    // 일감 추가 권한을 검증한다.
    private void validateCreatePermission(String projectId, String userId) {
        int permissionCount = issuesMapper.countIssueCreatePermission(projectId, userId);

        if (permissionCount == 0) {
            throw new IllegalArgumentException("일감 추가 권한이 없습니다.");
        }
    }

    // 기본 필수값을 검증한다.
    private void validateBasicIssue(IssuesVO issue) {
        if (issue == null) {
            throw new IllegalArgumentException("등록할 일감 정보가 없습니다.");
        }

        if (isBlank(issue.getProjectId())) {
            throw new IllegalArgumentException("프로젝트를 선택해주세요.");
        }

        if (isBlank(issue.getIssueTypeId())) {
            throw new IllegalArgumentException("일감 유형을 선택해주세요.");
        }

        if (isBlank(issue.getVersionId())) {
            throw new IllegalArgumentException("버전을 선택해주세요.");
        }

        if (isBlank(issue.getSettingCodeId())) {
            throw new IllegalArgumentException("우선순위를 선택해주세요.");
        }

        if (isBlank(issue.getTitle())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
    }

    // 선택한 버전이 생성 가능한 상태인지 검증한다.
    private void validateVersion(String projectId, String versionId) {
        int versionCount = issuesMapper.countVersionForCreate(projectId, versionId);

        if (versionCount == 0) {
            throw new IllegalArgumentException("선택한 버전은 현재 일감 생성에 사용할 수 없습니다.");
        }
    }

    // 선택한 우선순위 코드값을 검증한다.
    private void validatePriority(String projectId, String settingCodeId) {
        int priorityCount = issuesMapper.countPriorityForProject(projectId, settingCodeId);

        if (priorityCount == 0) {
            throw new IllegalArgumentException("선택한 우선순위를 사용할 수 없습니다.");
        }
    }

    // 제목/설명 길이를 검증한다.
    private void validateLength(IssuesVO issue) {
        if (issue.getTitle().length() > 255) {
            throw new IllegalArgumentException("제목은 255자 이하로 입력해주세요.");
        }

        if (issue.getDescription() != null && issue.getDescription().length() > 1000) {
            throw new IllegalArgumentException("설명은 1000자 이하로 입력해주세요.");
        }
    }

    // 진척도 값을 검증한다.
    private void validateProgressRate(IssuesVO issue) {
        if (issue.getProgressRate() == null) {
            issue.setProgressRate(0);
        }

        if (issue.getProgressRate() < 0 || issue.getProgressRate() > 100) {
            throw new IllegalArgumentException("진척도는 0부터 100 사이로 입력해주세요.");
        }
    }

    // 일감유형의 표준 항목 설정에 따라 동적 필드를 검증한다.
    private void validateDynamicFields(IssuesVO issue) {
        Map<String, IssuesVO> fieldSettingMap = getFieldSettingMap(issue.getIssueTypeId());

        validateAssignee(issue, fieldSettingMap);
        validateMilestone(issue, fieldSettingMap);
        validateParentIssue(issue, fieldSettingMap);
        validateSchedule(issue, fieldSettingMap);
        validateEstimatedHours(issue, fieldSettingMap);
    }

    private Map<String, IssuesVO> getFieldSettingMap(String issueTypeId) {
        List<IssuesVO> fieldSettingList = issuesMapper.selectFieldSettingListByIssueType(issueTypeId);
        Map<String, IssuesVO> fieldSettingMap = new HashMap<>();

        for (IssuesVO fieldSetting : fieldSettingList) {
            fieldSettingMap.put(fieldSetting.getFieldCode(), fieldSetting);
        }

        return fieldSettingMap;
    }

    private void validateAssignee(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
        if (!isFieldUsed(fieldSettingMap, FIELD_ASSIGNEE)) {
            issue.setAssigneeId(null);
            return;
        }

        if (isFieldRequired(fieldSettingMap, FIELD_ASSIGNEE) && isBlank(issue.getAssigneeId())) {
            throw new IllegalArgumentException("담당자를 선택해주세요.");
        }

        if (!isBlank(issue.getAssigneeId())
                && issuesMapper.countAssigneeForProject(issue.getProjectId(), issue.getAssigneeId()) == 0) {
            throw new IllegalArgumentException("선택한 담당자는 프로젝트 구성원이 아닙니다.");
        }
    }

    private void validateMilestone(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
        if (!isFieldUsed(fieldSettingMap, FIELD_MILESTONE)) {
            issue.setMilestoneId(null);
            return;
        }

        if (isFieldRequired(fieldSettingMap, FIELD_MILESTONE) && isBlank(issue.getMilestoneId())) {
            throw new IllegalArgumentException("마일스톤을 선택해주세요.");
        }

        if (!isBlank(issue.getMilestoneId())
                && issuesMapper.countMilestoneForCreate(
                        issue.getProjectId(),
                        issue.getVersionId(),
                        issue.getMilestoneId()
                ) == 0) {
            throw new IllegalArgumentException("선택한 마일스톤은 선택한 버전에 속하지 않습니다.");
        }
    }

    private void validateParentIssue(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
        if (!isFieldUsed(fieldSettingMap, FIELD_PARENT_ISSUE)) {
            issue.setParentIssueId(null);
            return;
        }

        if (isFieldRequired(fieldSettingMap, FIELD_PARENT_ISSUE) && isBlank(issue.getParentIssueId())) {
            throw new IllegalArgumentException("상위 일감을 선택해주세요.");
        }

        if (!isBlank(issue.getParentIssueId())
                && issuesMapper.countParentIssueForProject(issue.getProjectId(), issue.getParentIssueId()) == 0) {
            throw new IllegalArgumentException("선택한 상위 일감을 사용할 수 없습니다.");
        }
    }

    private void validateSchedule(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
        if (!isFieldUsed(fieldSettingMap, FIELD_START_DATE)) {
            issue.setStartDate(null);
        } else if (isFieldRequired(fieldSettingMap, FIELD_START_DATE) && issue.getStartDate() == null) {
            throw new IllegalArgumentException("시작일을 입력해주세요.");
        }

        if (!isFieldUsed(fieldSettingMap, FIELD_DUE_DATE)) {
            issue.setDueDate(null);
        } else if (isFieldRequired(fieldSettingMap, FIELD_DUE_DATE) && issue.getDueDate() == null) {
            throw new IllegalArgumentException("완료일을 입력해주세요.");
        }

        if (issue.getStartDate() != null
                && issue.getDueDate() != null
                && issue.getDueDate().isBefore(issue.getStartDate())) {
            throw new IllegalArgumentException("완료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validateEstimatedHours(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
        if (!isFieldUsed(fieldSettingMap, FIELD_ESTIMATED_HOURS)) {
            issue.setEstimatedHours(null);
            return;
        }

        if (isFieldRequired(fieldSettingMap, FIELD_ESTIMATED_HOURS) && issue.getEstimatedHours() == null) {
            throw new IllegalArgumentException("추정시간을 입력해주세요.");
        }

        if (issue.getEstimatedHours() == null) {
            issue.setEstimatedHours(0);
        }

        if (issue.getEstimatedHours() < 0 || issue.getEstimatedHours() > 99999) {
            throw new IllegalArgumentException("추정시간은 0부터 99999 사이로 입력해주세요.");
        }
    }

    private boolean isFieldUsed(Map<String, IssuesVO> fieldSettingMap, String fieldCode) {
        IssuesVO fieldSetting = fieldSettingMap.get(fieldCode);
        return fieldSetting != null && "Y".equals(fieldSetting.getUseYn());
    }

    private boolean isFieldRequired(Map<String, IssuesVO> fieldSettingMap, String fieldCode) {
        IssuesVO fieldSetting = fieldSettingMap.get(fieldCode);
        return fieldSetting != null && "Y".equals(fieldSetting.getRequiredYn());
    }

    private void validateUserId(String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    private String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
