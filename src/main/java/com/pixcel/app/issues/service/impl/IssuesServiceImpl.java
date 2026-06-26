package com.pixcel.app.issues.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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

	private static final String PERMISSION_ISSUE_CREATE_CODE = "p008";
	private static final String PERMISSION_ISSUE_CREATE_NAME = "일감 추가";

	private static final String PERMISSION_ISSUE_DELETE_CODE = "p010";
	private static final String PERMISSION_ISSUE_DELETE_NAME = "일감 삭제";

	private static final String PERMISSION_MILESTONE_CREATE_CODE = "p005";
	private static final String PERMISSION_MILESTONE_CREATE_NAME = "마일스톤 생성";

	// 페이징
	private static final int ISSUE_PAGE_SIZE = 10;

	@Override
	public List<IssuesVO> getProjectList(String userId) {
		validateUserId(userId);
		return issuesMapper.selectProjectList(userId);
	}

	@Override
	public String getSelectedProjectId(String requestedProjectId, String userId) {
		validateUserId(userId);

		if (isBlank(requestedProjectId)) {
			return null;
		}

		validateProjectAccess(requestedProjectId, userId);
		return requestedProjectId;
	}

	@Override
	public IssuesVO getProjectDetail(String projectId, String userId) {
		return validateProjectAccess(projectId, userId);
	}

	@Override
	public List<IssuesVO> getIssueList(String projectId, IssuesVO searchVO, String userId) {
		validateProjectAccess(projectId, userId);

		if (searchVO == null) {
			searchVO = new IssuesVO();
		}

		searchVO.setProjectId(projectId);
		trimSearchCondition(searchVO);
		applyPaging(searchVO);

		int totalCount = issuesMapper.countIssueList(searchVO);
		int totalPage = (int) Math.ceil((double) totalCount / ISSUE_PAGE_SIZE);

		searchVO.setTotalCount(totalCount);
		searchVO.setTotalPage(totalPage);

		if (totalPage > 0 && searchVO.getPage() > totalPage) {
			searchVO.setPage(totalPage);
			setPagingRange(searchVO);
		}

		return issuesMapper.selectIssueList(searchVO);
	}

	@Override
	public List<IssuesVO> getIssueStatusList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectIssueStatusList(projectId);
	}

	@Override
	public boolean canCreateIssue(String projectId, String userId) {
		validateProjectAccess(projectId, userId);

		return hasProjectPermission(projectId, userId, PERMISSION_ISSUE_CREATE_CODE, PERMISSION_ISSUE_CREATE_NAME);
	}

	@Override
	public boolean canCreateMilestone(String projectId, String userId) {
		validateProjectAccess(projectId, userId);

		return hasProjectPermission(projectId, userId, PERMISSION_MILESTONE_CREATE_CODE,
				PERMISSION_MILESTONE_CREATE_NAME);
	}

	@Override
	public boolean canDeleteIssue(String projectId, String userId) {
		validateProjectAccess(projectId, userId);

		return hasProjectPermission(projectId, userId, PERMISSION_ISSUE_DELETE_CODE, PERMISSION_ISSUE_DELETE_NAME);
	}

	@Override
	@Transactional
	public void deleteIssue(String projectId, String issueId, String userId) {
		validateProjectAccess(projectId, userId);

		if (isBlank(issueId)) {
			throw new IllegalArgumentException("삭제할 일감 정보가 없습니다.");
		}

		if (!canDeleteIssue(projectId, userId)) {
			throw new IllegalArgumentException("일감 삭제 권한이 없습니다.");
		}

		if (issuesMapper.countChildIssue(projectId, issueId) > 0) {
			throw new IllegalArgumentException("다른 일감의 상위일감으로 사용 중인 일감은 삭제할 수 없습니다.");
		}

		try {
			int deleteCount = issuesMapper.deleteIssue(projectId, issueId);

			if (deleteCount == 0) {
				throw new IllegalArgumentException("삭제할 일감이 없습니다.");
			}
		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("연결된 데이터가 있는 일감은 삭제할 수 없습니다.");
		}
	}

	@Override
	public IssuesVO getProjectDetailForCreate(String projectId, String userId) {
		IssuesVO project = validateProjectAccess(projectId, userId);
		validateCreatePermission(projectId, userId);

		return project;
	}

	@Override
	public List<IssuesVO> getIssueTypeList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectIssueTypeList(projectId);
	}

	@Override
	public List<IssuesVO> getFieldSettingList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectFieldSettingListByProject(projectId);
	}

	@Override
	public List<IssuesVO> getVersionList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectVersionList(projectId);
	}

	@Override
	public List<IssuesVO> getMilestoneList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectMilestoneList(projectId);
	}

	@Override
	public List<IssuesVO> getPriorityList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectPriorityList(projectId);
	}

	@Override
	public List<IssuesVO> getAssigneeList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectAssigneeList(projectId);
	}

	@Override
	public List<IssuesVO> getParentIssueList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectParentIssueList(projectId);
	}

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

		issuesMapper.selectProjectForUpdate(issue.getProjectId());
		issue.setIssueNo(issuesMapper.selectNextIssueNo(issue.getProjectId()));

		issuesMapper.insertIssue(issue);
	}

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

	private void validateCreatePermission(String projectId, String userId) {
		if (!hasProjectPermission(projectId, userId, PERMISSION_ISSUE_CREATE_CODE, PERMISSION_ISSUE_CREATE_NAME)) {
			throw new IllegalArgumentException("일감 추가 권한이 없습니다.");
		}
	}

	private boolean hasProjectPermission(String projectId, String userId, String permissionCode,
			String permissionName) {

		validateUserId(userId);

		if (isBlank(projectId)) {
			throw new IllegalArgumentException("프로젝트를 선택해주세요.");
		}

		String checkedPermissionCode = trimToNull(permissionCode);
		String checkedPermissionName = trimToNull(permissionName);

		if (checkedPermissionCode == null && checkedPermissionName == null) {
			throw new IllegalArgumentException("확인할 권한 정보가 없습니다.");
		}

		return issuesMapper.countProjectPermission(projectId, userId, checkedPermissionCode, checkedPermissionName) > 0;
	}

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

	private void validateVersion(String projectId, String versionId) {
		int versionCount = issuesMapper.countVersionForCreate(projectId, versionId);

		if (versionCount == 0) {
			throw new IllegalArgumentException("선택한 버전은 현재 일감 생성에 사용할 수 없습니다.");
		}
	}

	private void validatePriority(String projectId, String settingCodeId) {
		int priorityCount = issuesMapper.countPriorityForProject(projectId, settingCodeId);

		if (priorityCount == 0) {
			throw new IllegalArgumentException("선택한 우선순위를 사용할 수 없습니다.");
		}
	}

	private void validateLength(IssuesVO issue) {
		if (issue.getTitle().length() > 255) {
			throw new IllegalArgumentException("제목은 255자 이하로 입력해주세요.");
		}

		if (issue.getDescription() != null && issue.getDescription().length() > 1000) {
			throw new IllegalArgumentException("설명은 1000자 이하로 입력해주세요.");
		}
	}

	private void validateProgressRate(IssuesVO issue) {
		if (issue.getProgressRate() == null) {
			issue.setProgressRate(0);
		}

		if (issue.getProgressRate() < 0 || issue.getProgressRate() > 100) {
			throw new IllegalArgumentException("진척도는 0부터 100 사이로 입력해주세요.");
		}
	}

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

		if (!isBlank(issue.getMilestoneId()) && issuesMapper.countMilestoneForCreate(issue.getProjectId(),
				issue.getVersionId(), issue.getMilestoneId()) == 0) {
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

		if (issue.getStartDate() != null && issue.getDueDate() != null
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

	private void trimSearchCondition(IssuesVO searchVO) {
		searchVO.setKeyword(trimToNull(searchVO.getKeyword()));
		searchVO.setIssueTypeId(trimToNull(searchVO.getIssueTypeId()));
		searchVO.setVersionId(trimToNull(searchVO.getVersionId()));
		searchVO.setIssueStatusId(trimToNull(searchVO.getIssueStatusId()));
		searchVO.setSettingCodeId(trimToNull(searchVO.getSettingCodeId()));
		searchVO.setAssigneeId(trimToNull(searchVO.getAssigneeId()));
		searchVO.setIssueNoSort(normalizeIssueNoSort(searchVO.getIssueNoSort()));
	}

	private void applyPaging(IssuesVO searchVO) {
		if (searchVO.getPage() == null || searchVO.getPage() < 1) {
			searchVO.setPage(1);
		}

		searchVO.setPageSize(ISSUE_PAGE_SIZE);
		setPagingRange(searchVO);
	}

	private void setPagingRange(IssuesVO searchVO) {
		int page = searchVO.getPage();
		int pageSize = searchVO.getPageSize();

		searchVO.setStartRow((page - 1) * pageSize + 1);
		searchVO.setEndRow(page * pageSize);
	}

	private String normalizeIssueNoSort(String issueNoSort) {
		String sort = trimToNull(issueNoSort);

		if ("asc".equalsIgnoreCase(sort)) {
			return "asc";
		}

		return "desc";
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