package com.pixcel.app.issues.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.issues.mapper.IssuesMapper;
import com.pixcel.app.issues.service.IssueHistoryGroupVO;
import com.pixcel.app.issues.service.IssuesService;
import com.pixcel.app.issues.service.IssuesVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssuesServiceImpl implements IssuesService {

	private final IssuesMapper issuesMapper;

	private static final String FIELD_ISSUE = "ISSUE";
	private static final String FIELD_TITLE = "TITLE";
	private static final String FIELD_DESCRIPTION = "DESCRIPTION";
	private static final String FIELD_STATUS = "STATUS";
	private static final String FIELD_PRIORITY = "PRIORITY";
	private static final String FIELD_ASSIGNEE = "ASSIGNEE";
	private static final String FIELD_PROGRESS_RATE = "PROGRESS_RATE";
	private static final String FIELD_MILESTONE = "MILESTONE";
	private static final String FIELD_PARENT_ISSUE = "PARENT_ISSUE";
	private static final String FIELD_START_DATE = "START_DATE";
	private static final String FIELD_DUE_DATE = "DUE_DATE";
	private static final String FIELD_ESTIMATED_HOURS = "ESTIMATED_HOURS";
	private static final String FIELD_ATTACH_FILE = "ATTACH_FILE";

	private static final String OPTION_ISSUE_TYPE = "ISSUE_TYPE";
	private static final String OPTION_ISSUE_STATUS = "ISSUE_STATUS";
	private static final String OPTION_FIELD_SETTING = "FIELD_SETTING";
	private static final String OPTION_AVAILABLE_STATUS = "AVAILABLE_STATUS";
	private static final String OPTION_VERSION = "VERSION";
	private static final String OPTION_MILESTONE = "MILESTONE";
	private static final String OPTION_PRIORITY = "PRIORITY";
	private static final String OPTION_ASSIGNEE = "ASSIGNEE";
	private static final String OPTION_PARENT_ISSUE = "PARENT_ISSUE";

	private static final String PERMISSION_ISSUE_CREATE_CODE = "p008";
	private static final String PERMISSION_ISSUE_CREATE_NAME = "일감 추가";

	private static final String PERMISSION_ISSUE_UPDATE_CODE = "p009";
	private static final String PERMISSION_ISSUE_UPDATE_OWN_CODE = "p011";

	private static final String PERMISSION_ISSUE_DELETE_CODE = "p010";
	private static final String PERMISSION_ISSUE_DELETE_NAME = "일감 삭제";

	private static final String PERMISSION_MILESTONE_CREATE_CODE = "p005";
	private static final String PERMISSION_MILESTONE_CREATE_NAME = "마일스톤 생성";

	private static final String CHANGE_TYPE_CREATE_CODE = "M001";
	private static final String CHANGE_TYPE_CREATE_NAME = "일감 생성";
	private static final String CHANGE_TYPE_UPDATE_CODE = "M002";
	private static final String CHANGE_TYPE_UPDATE_NAME = "일감 수정";
	private static final String CHANGE_TYPE_DELETE_CODE = "M003";
	private static final String CHANGE_TYPE_DELETE_NAME = "일감 삭제";
	private static final String CHANGE_TYPE_FILE_ADD_CODE = "M004";
	private static final String CHANGE_TYPE_FILE_DELETE_CODE = "M005";

	private static final Map<String, String> CHANGE_TYPE_DISPLAY_NAME_MAP = createChangeTypeDisplayNameMap();
	private static final Map<String, String> FIELD_NAME_MAP = createFieldNameMap();

	// 페이징
	private static final int ISSUE_PAGE_SIZE = 10;
	private static final int ISSUE_PAGE_BLOCK_SIZE = 5;

	private static Map<String, String> createChangeTypeDisplayNameMap() {
		Map<String, String> displayNameMap = new HashMap<>();
		displayNameMap.put(CHANGE_TYPE_CREATE_CODE, CHANGE_TYPE_CREATE_NAME);
		displayNameMap.put(CHANGE_TYPE_UPDATE_CODE, CHANGE_TYPE_UPDATE_NAME);
		displayNameMap.put(CHANGE_TYPE_DELETE_CODE, CHANGE_TYPE_DELETE_NAME);
		displayNameMap.put(CHANGE_TYPE_FILE_ADD_CODE, "파일 추가");
		displayNameMap.put(CHANGE_TYPE_FILE_DELETE_CODE, "파일 삭제");
		return Collections.unmodifiableMap(displayNameMap);
	}

	private static Map<String, String> createFieldNameMap() {
		Map<String, String> fieldNameMap = new HashMap<>();
		fieldNameMap.put(FIELD_ISSUE, "일감");
		fieldNameMap.put(FIELD_TITLE, "제목");
		fieldNameMap.put(FIELD_DESCRIPTION, "설명");
		fieldNameMap.put(FIELD_STATUS, "상태");
		fieldNameMap.put(FIELD_PRIORITY, "우선순위");
		fieldNameMap.put(FIELD_ASSIGNEE, "담당자");
		fieldNameMap.put(FIELD_PROGRESS_RATE, "진척도");
		fieldNameMap.put(FIELD_ESTIMATED_HOURS, "추정시간");
		fieldNameMap.put(FIELD_START_DATE, "시작일");
		fieldNameMap.put(FIELD_DUE_DATE, "완료기한");
		fieldNameMap.put(FIELD_MILESTONE, "마일스톤");
		fieldNameMap.put(FIELD_PARENT_ISSUE, "상위일감");
		fieldNameMap.put(FIELD_ATTACH_FILE, "첨부파일");
		return Collections.unmodifiableMap(fieldNameMap);
	}

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

		return selectIssueList(projectId, searchVO);
	}

	@Override
	public Map<String, Object> getIssueListPageData(String projectId, IssuesVO searchVO, String userId) {
		IssuesVO projectInfo = issuesMapper.selectIssueListProjectAccess(projectId, userId,
				PERMISSION_ISSUE_CREATE_CODE, PERMISSION_MILESTONE_CREATE_CODE);

		validateIssueListAccess(projectInfo);

		List<IssuesVO> issueList = selectIssueList(projectId, searchVO);
		Map<String, Object> selectedFilterData = buildSelectedIssueListFilterData(searchVO);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", projectInfo);
		pageData.put("issueList", issueList);
		pageData.put("issueTypeList", Collections.emptyList());
		pageData.put("issueStatusList", Collections.emptyList());
		pageData.put("versionList", Collections.emptyList());
		pageData.put("priorityList", Collections.emptyList());
		pageData.put("assigneeList", Collections.emptyList());
		pageData.putAll(selectedFilterData);
		pageData.put("canCreateIssue", "Y".equals(projectInfo.getIssueCreatePermissionYn()));
		pageData.put("canCreateMilestone", "Y".equals(projectInfo.getMilestoneCreatePermissionYn()));

		return pageData;
	}

	@Override
	public Map<String, Object> getIssueListFilterData(String projectId, String userId) {
		validateProjectAccess(projectId, userId);

		return buildIssueListFilterData(projectId);
	}

	private Map<String, Object> buildIssueListFilterData(String projectId) {
		List<IssuesVO> optionRows = issuesMapper.selectIssueListOptionRows(projectId);

		Map<String, Object> filterData = new HashMap<>();
		filterData.put("issueTypeList", filterOptionRows(optionRows, OPTION_ISSUE_TYPE));
		filterData.put("issueStatusList", filterOptionRows(optionRows, OPTION_ISSUE_STATUS));
		filterData.put("versionList", filterOptionRows(optionRows, OPTION_VERSION));
		filterData.put("priorityList", filterOptionRows(optionRows, OPTION_PRIORITY));
		filterData.put("assigneeList", filterOptionRows(optionRows, OPTION_ASSIGNEE));

		return filterData;
	}

	private Map<String, Object> buildSelectedIssueListFilterData(IssuesVO searchVO) {
		boolean hasSelectedFilter = hasSelectedIssueListFilter(searchVO);
		List<IssuesVO> selectedOptionRows = hasSelectedFilter ? issuesMapper.selectIssueListSelectedOptionRows(searchVO)
				: Collections.emptyList();

		Map<String, Object> filterData = new HashMap<>();
		filterData.put("selectedIssueTypeList", filterOptionRows(selectedOptionRows, OPTION_ISSUE_TYPE));
		filterData.put("selectedIssueStatusList", filterOptionRows(selectedOptionRows, OPTION_ISSUE_STATUS));
		filterData.put("selectedVersionList", filterOptionRows(selectedOptionRows, OPTION_VERSION));
		filterData.put("selectedMilestoneList", filterOptionRows(selectedOptionRows, OPTION_MILESTONE));
		filterData.put("selectedPriorityList", filterOptionRows(selectedOptionRows, OPTION_PRIORITY));
		filterData.put("selectedAssigneeList", filterOptionRows(selectedOptionRows, OPTION_ASSIGNEE));

		return filterData;
	}

	private boolean hasSelectedIssueListFilter(IssuesVO searchVO) {
		if (searchVO == null) {
			return false;
		}

		return hasValue(searchVO.getIssueTypeIdList()) || hasValue(searchVO.getIssueStatusIdList())
				|| hasValue(searchVO.getVersionIdList()) || hasValue(searchVO.getMilestoneIdList())
				|| hasValue(searchVO.getSettingCodeIdList()) || hasValue(searchVO.getAssigneeIdList());
	}

	private boolean hasValue(List<String> valueList) {
		return valueList != null && !valueList.isEmpty();
	}

	private List<IssuesVO> selectIssueList(String projectId, IssuesVO searchVO) {
		if (searchVO == null) {
			searchVO = new IssuesVO();
		}

		searchVO.setProjectId(projectId);
		trimSearchCondition(searchVO);
		applyPaging(searchVO);

		int currentStartRow = getIssuePageStartRow(searchVO.getPage());
		int currentEndRow = currentStartRow + ISSUE_PAGE_SIZE;
		List<IssuesVO> pageBlockRows = issuesMapper.selectIssueList(searchVO);
		if (pageBlockRows == null) {
			pageBlockRows = Collections.emptyList();
		}

		applyPageProbeResult(searchVO, pageBlockRows, currentStartRow, currentEndRow);

		return pageBlockRows.stream().filter(row -> row.getRowNo() != null)
				.filter(row -> row.getRowNo() >= currentStartRow && row.getRowNo() < currentEndRow)
				.collect(Collectors.toList());
	}

	@Override
	public List<IssuesVO> getIssueStatusList(String projectId, String userId) {
		validateProjectAccess(projectId, userId);
		return issuesMapper.selectIssueStatusList(projectId);
	}

	@Override
	public Map<String, Object> getIssueReportPageData(String projectId, String userId) {
		IssuesVO projectInfo = validateProjectAccess(projectId, userId);

		List<IssuesVO> reportRows = issuesMapper.selectIssueReportRows(projectId);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", projectInfo);
		pageData.put("issueTypeReportList", filterOptionRows(reportRows, OPTION_ISSUE_TYPE));
		pageData.put("versionReportList", filterOptionRows(reportRows, OPTION_VERSION));
		pageData.put("priorityReportList", filterOptionRows(reportRows, OPTION_PRIORITY));
		pageData.put("assigneeReportList", filterOptionRows(reportRows, OPTION_ASSIGNEE));
		pageData.put("milestoneReportList", filterOptionRows(reportRows, OPTION_MILESTONE));

		return pageData;
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
	public Map<String, Object> getIssueDetailPageData(String projectId, String issueId, String userId) {
		validateUserId(userId);

		if (isBlank(issueId)) {
			throw new IllegalArgumentException("조회할 일감 정보가 없습니다.");
		}

		IssuesVO issue = issuesMapper.selectIssueDetail(projectId, issueId, userId, PERMISSION_ISSUE_UPDATE_CODE,
				PERMISSION_ISSUE_UPDATE_OWN_CODE, PERMISSION_ISSUE_DELETE_CODE);

		if (issue == null) {
			IssuesVO projectInfo = validateProjectAccess(projectId, userId);
			List<IssuesVO> historyRows = issuesMapper.selectIssueHistoryRows(issueId);

			if (historyRows == null || historyRows.isEmpty()) {
				throw new IllegalArgumentException("조회할 수 있는 일감이 없습니다.");
			}

			return buildDeletedIssueDetailPageData(projectInfo, issueId, historyRows);
		}

		List<IssuesVO> historyRows = issuesMapper.selectIssueHistoryRows(issueId);
		// 상세 화면의 선택 목록과 유형별 항목 설정을 한 번에 조회해 DB 왕복을 줄인다.
		List<IssuesVO> optionRows = issuesMapper.selectIssueDetailRows(projectId, issueId, userId);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("issue", issue);
		pageData.put("fieldSettingList", filterOptionRows(optionRows, OPTION_FIELD_SETTING));
		pageData.put("priorityList", filterOptionRows(optionRows, OPTION_PRIORITY));
		pageData.put("assigneeList", filterOptionRows(optionRows, OPTION_ASSIGNEE));
		pageData.put("milestoneList", filterOptionRows(optionRows, OPTION_MILESTONE));
		pageData.put("parentIssueList", filterOptionRows(optionRows, OPTION_PARENT_ISSUE));
		pageData.put("availableStatusList",
				distinctByIssueStatusId(filterOptionRows(optionRows, OPTION_AVAILABLE_STATUS)));
		pageData.put("historyList", historyRows);
		pageData.put("historyGroupList", buildHistoryGroupList(historyRows));
		pageData.put("deletedIssue", false);
		pageData.put("canUpdateIssue", "Y".equals(issue.getIssueUpdatePermissionYn()));
		pageData.put("canDeleteIssue", "Y".equals(issue.getIssueDeletePermissionYn()));

		return pageData;
	}

	@Override
	public void validateIssueAccess(String projectId, String issueId, String userId) {
		validateUserId(userId);

		if (isBlank(projectId) || isBlank(issueId)) {
			throw new IllegalArgumentException("조회할 일감 정보가 없습니다.");
		}

		int accessCount = issuesMapper.countIssueDetailAccess(projectId, issueId, userId);

		if (accessCount == 0) {
			throw new IllegalArgumentException("조회할 수 있는 일감이 없습니다.");
		}
	}

	private void validateIssueFileManageAccess(String projectId, String issueId, String userId) {
		validateIssueAccess(projectId, issueId, userId);

		if (!hasIssueUpdatePermission(projectId, issueId, userId)) {
			throw new IllegalArgumentException("일감 수정 권한이 없습니다.");
		}
	}

	@Override
	@Transactional
	public void updateIssue(IssuesVO issue, String userId) {
		validateUserId(userId);
		validateBasicIssueForUpdate(issue);
		validateIssueUpdatePermission(issue.getProjectId(), issue.getIssueId(), userId);

		IssuesVO currentIssue = issuesMapper.selectIssueForUpdate(issue.getProjectId(), issue.getIssueId());

		if (currentIssue == null) {
			throw new IllegalArgumentException("수정할 일감이 없습니다.");
		}

		normalizeIssueForUpdate(issue, currentIssue);
		validateLength(issue);
		validateProgressRate(issue);
		validatePriority(issue.getProjectId(), issue.getSettingCodeId());
		validateDynamicFieldsForUpdate(issue);
		validateStatusTransition(currentIssue, issue, userId);

		int updateCount = issuesMapper.updateIssue(issue);

		if (updateCount == 0) {
			throw new IllegalArgumentException("수정할 일감이 없습니다.");
		}

		IssuesVO updatedIssue = issuesMapper.selectIssueForUpdate(issue.getProjectId(), issue.getIssueId());
		recordUpdateHistory(currentIssue, updatedIssue, userId, issue.getHistoryReason());
	}

	@Override
	@Transactional
	public void recordIssueFileAddHistory(String issueId, String userId, int uploadCount) {
		validateUserId(userId);

		if (uploadCount <= 0) {
			return;
		}

		insertIssueHistory(newHistoryGroupId(), issueId, userId, CHANGE_TYPE_FILE_ADD_CODE, FIELD_ATTACH_FILE, "파일 추가",
				"-", "첨부파일 " + uploadCount + "개 추가");
	}

	@Override
	@Transactional
	public void deleteIssueFile(String projectId, String issueId, String fileId, String userId) {
		validateIssueFileManageAccess(projectId, issueId, userId);

		if (isBlank(fileId)) {
			throw new IllegalArgumentException("삭제할 첨부파일 정보가 없습니다.");
		}

		FileVO file = issuesMapper.selectIssueFile(issueId, fileId);

		if (file == null) {
			throw new IllegalArgumentException("삭제할 첨부파일이 없습니다.");
		}

		int deleteCount = issuesMapper.deleteIssueFile(issueId, fileId);

		if (deleteCount == 0) {
			throw new IllegalArgumentException("삭제할 첨부파일이 없습니다.");
		}

		deletePhysicalFile(file.getFilePath());
		insertIssueHistory(newHistoryGroupId(), issueId, userId, CHANGE_TYPE_FILE_DELETE_CODE, FIELD_ATTACH_FILE,
				"파일 삭제", emptyToDash(file.getOriginalName()), "삭제됨");
	}

	@Override
	@Transactional
	public int deleteIssueFiles(String projectId, String issueId, List<String> fileIds, String userId) {
		List<String> checkedFileIds = fileIds == null ? Collections.emptyList()
				: fileIds.stream().map(this::trimToNull).filter(Objects::nonNull).distinct()
						.collect(Collectors.toList());

		if (checkedFileIds.isEmpty()) {
			return 0;
		}

		validateIssueFileManageAccess(projectId, issueId, userId);

		int deleteCount = 0;

		for (String fileId : checkedFileIds) {
			FileVO file = issuesMapper.selectIssueFile(issueId, fileId);

			if (file == null) {
				throw new IllegalArgumentException("삭제할 첨부파일이 없습니다.");
			}

			int currentDeleteCount = issuesMapper.deleteIssueFile(issueId, fileId);

			if (currentDeleteCount == 0) {
				throw new IllegalArgumentException("삭제할 첨부파일이 없습니다.");
			}

			deletePhysicalFile(file.getFilePath());

			insertIssueHistory(newHistoryGroupId(), issueId, userId, CHANGE_TYPE_FILE_DELETE_CODE, FIELD_ATTACH_FILE,
					"파일 삭제", emptyToDash(file.getOriginalName()), "삭제됨");
			deleteCount += currentDeleteCount;
		}

		return deleteCount;
	}

	@Override
	@Transactional
	public void deleteIssue(String projectId, String issueId, String userId) {
		validateProjectPermissionAccess(projectId, userId, PERMISSION_ISSUE_DELETE_CODE,
				"일감 삭제 권한이 없습니다.");

		if (isBlank(issueId)) {
			throw new IllegalArgumentException("삭제할 일감 정보가 없습니다.");
		}

		if (issuesMapper.countChildIssue(projectId, issueId) > 0) {
			throw new IllegalArgumentException("다른 일감의 상위일감으로 사용 중인 일감은 삭제할 수 없습니다.");
		}

		IssuesVO deleteTarget = issuesMapper.selectIssueForUpdate(projectId, issueId);

		if (deleteTarget == null) {
			throw new IllegalArgumentException("삭제할 일감이 없습니다.");
		}

		try {
			insertIssueHistory(newHistoryGroupId(), issueId, userId, CHANGE_TYPE_DELETE_CODE, FIELD_ISSUE,
					CHANGE_TYPE_DELETE_NAME, displayIssueLabel(deleteTarget), "삭제됨");

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
		return validateProjectPermissionAccess(projectId, userId, PERMISSION_ISSUE_CREATE_CODE,
				"일감 추가 권한이 없습니다.");
	}

	@Override
	public Map<String, Object> getIssueCreatePageData(String projectId, String userId) {
		IssuesVO projectInfo = issuesMapper.selectIssueCreateProjectAccess(projectId, userId,
				PERMISSION_ISSUE_CREATE_CODE);

		validateIssueCreatePermission(projectInfo);

		List<IssuesVO> optionRows = issuesMapper.selectIssueCreateBaseOptionRows(projectId);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", projectInfo);
		pageData.put("issueTypeList", filterOptionRows(optionRows, OPTION_ISSUE_TYPE));
		pageData.put("fieldSettingList", filterOptionRows(optionRows, OPTION_FIELD_SETTING));
		pageData.put("versionList", filterOptionRows(optionRows, OPTION_VERSION));
		pageData.put("priorityList", filterOptionRows(optionRows, OPTION_PRIORITY));
		pageData.put("milestoneList", Collections.emptyList());
		pageData.put("assigneeList", Collections.emptyList());
		pageData.put("parentIssueList", Collections.emptyList());

		return pageData;
	}

	@Override
	public Map<String, Object> getIssueCreateExtraOptionData(String projectId, String userId) {
		IssuesVO projectInfo = issuesMapper.selectIssueCreateProjectAccess(projectId, userId,
				PERMISSION_ISSUE_CREATE_CODE);

		validateIssueCreatePermission(projectInfo);

		List<IssuesVO> optionRows = issuesMapper.selectIssueCreateExtraOptionRows(projectId);

		Map<String, Object> optionData = new HashMap<>();
		optionData.put("milestoneList", filterOptionRows(optionRows, OPTION_MILESTONE));
		optionData.put("assigneeList", filterOptionRows(optionRows, OPTION_ASSIGNEE));
		optionData.put("parentIssueList", filterOptionRows(optionRows, OPTION_PARENT_ISSUE));

		return optionData;
	}

	private void validateIssueCreatePermission(IssuesVO projectInfo) {
		if (projectInfo == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}

		if (!"Y".equals(projectInfo.getPermissionYn())) {
			throw new IllegalArgumentException("일감 추가 권한이 없습니다.");
		}
	}

	private void validateIssueListAccess(IssuesVO projectInfo) {
		if (projectInfo == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}
	}

	private List<IssuesVO> filterOptionRows(List<IssuesVO> optionRows, String optionGroup) {
		if (optionRows == null || optionRows.isEmpty()) {
			return Collections.emptyList();
		}

		return optionRows.stream().filter(row -> optionGroup.equals(row.getOptionGroup())).collect(Collectors.toList());
	}

	private List<IssuesVO> distinctByIssueStatusId(List<IssuesVO> statusList) {
		if (statusList == null || statusList.isEmpty()) {
			return Collections.emptyList();
		}

		return statusList.stream().filter(status -> !isBlank(status.getIssueStatusId()))
				.collect(
						Collectors.collectingAndThen(
								Collectors.toMap(IssuesVO::getIssueStatusId, status -> status, (first, second) -> first,
										LinkedHashMap::new),
								map -> map.values().stream().collect(Collectors.toList())));
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

		IssuesVO validation = issuesMapper.selectIssueCreateSaveValidation(issue, userId, PERMISSION_ISSUE_CREATE_CODE);
		validateIssueCreateSaveCondition(validation);

		issue.setIssueStatusId(validation.getInitialStatusId());
		issue.setAuthorId(userId);
		issue.setTitle(issue.getTitle().trim());
		issue.setDescription(trimToNull(issue.getDescription()));
		issue.setAssigneeId(trimToNull(issue.getAssigneeId()));
		issue.setMilestoneId(trimToNull(issue.getMilestoneId()));
		issue.setParentIssueId(trimToNull(issue.getParentIssueId()));

		validateLength(issue);
		validateProgressRate(issue);
		validateDynamicFieldsForCreate(issue, validation);

		issue.setIssueNo(issuesMapper.selectNextIssueNoForUpdate(issue.getProjectId()));

		issuesMapper.insertIssue(issue);
		issue.setDisplayIssueNo(formatDisplayIssueNo(issue.getIssueNo()));
		insertIssueHistory(newHistoryGroupId(), issue.getIssueId(), userId, CHANGE_TYPE_CREATE_CODE, FIELD_ISSUE,
				CHANGE_TYPE_CREATE_NAME, "-", displayIssueLabel(issue));
	}

	private void validateBasicIssueForUpdate(IssuesVO issue) {
		if (issue == null) {
			throw new IllegalArgumentException("수정할 일감 정보가 없습니다.");
		}

		if (isBlank(issue.getProjectId())) {
			throw new IllegalArgumentException("프로젝트를 선택해주세요.");
		}

		if (isBlank(issue.getIssueId())) {
			throw new IllegalArgumentException("수정할 일감 정보가 없습니다.");
		}

		if (isBlank(issue.getIssueStatusId())) {
			throw new IllegalArgumentException("상태를 선택해주세요.");
		}

		if (isBlank(issue.getSettingCodeId())) {
			throw new IllegalArgumentException("우선순위를 선택해주세요.");
		}

		if (isBlank(issue.getTitle())) {
			throw new IllegalArgumentException("제목을 입력해주세요.");
		}
	}

	private void normalizeIssueForUpdate(IssuesVO issue, IssuesVO currentIssue) {
		issue.setIssueTypeId(currentIssue.getIssueTypeId());
		issue.setVersionId(currentIssue.getVersionId());
		issue.setTitle(issue.getTitle().trim());
		issue.setDescription(trimToNull(issue.getDescription()));
		issue.setAssigneeId(trimToNull(issue.getAssigneeId()));
		issue.setMilestoneId(trimToNull(issue.getMilestoneId()));
		issue.setParentIssueId(trimToNull(issue.getParentIssueId()));
		issue.setHistoryReason(trimToNull(issue.getHistoryReason()));

		if (issue.getIssueId().equals(issue.getParentIssueId())) {
			throw new IllegalArgumentException("자기 자신은 상위 일감으로 선택할 수 없습니다.");
		}
	}

	private void validateDynamicFieldsForUpdate(IssuesVO issue) {
		Map<String, IssuesVO> fieldSettingMap = getFieldSettingMap(issue.getIssueTypeId());

		validateAssignee(issue, fieldSettingMap);
		validateMilestoneForUpdate(issue, fieldSettingMap);
		validateParentIssue(issue, fieldSettingMap);
		validateSchedule(issue, fieldSettingMap);
		validateEstimatedHours(issue, fieldSettingMap);
	}

	private void validateMilestoneForUpdate(IssuesVO issue, Map<String, IssuesVO> fieldSettingMap) {
		if (!isFieldUsed(fieldSettingMap, FIELD_MILESTONE)) {
			issue.setMilestoneId(null);
			return;
		}

		if (isFieldRequired(fieldSettingMap, FIELD_MILESTONE) && isBlank(issue.getMilestoneId())) {
			throw new IllegalArgumentException("마일스톤을 선택해주세요.");
		}

		if (!isBlank(issue.getMilestoneId()) && issuesMapper.countMilestoneForUpdate(issue.getProjectId(),
				issue.getVersionId(), issue.getMilestoneId()) == 0) {
			throw new IllegalArgumentException("선택한 마일스톤은 해당 버전의 마일스톤이 아닙니다.");
		}
	}

	private void validateStatusTransition(IssuesVO currentIssue, IssuesVO issue, String userId) {
		if (Objects.equals(currentIssue.getIssueStatusId(), issue.getIssueStatusId())) {
			return;
		}

		int allowedCount = issuesMapper.countAllowedStatusTransition(issue.getProjectId(), issue.getIssueId(),
				issue.getIssueStatusId(), userId);

		if (allowedCount == 0) {
			throw new IllegalArgumentException("업무흐름에서 허용되지 않은 상태 변경입니다.");
		}
	}

	private void recordUpdateHistory(IssuesVO before, IssuesVO after, String userId, String historyReason) {
		if (after == null) {
			return;
		}

		String historyGroupId = newHistoryGroupId();
		String reason = defaultHistoryReason(historyReason, CHANGE_TYPE_UPDATE_NAME);
		List<IssuesVO> historyList = new ArrayList<>();

		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_STATUS,
				before.getIssueStatusId(), after.getIssueStatusId(), before.getIssueStatusName(),
				after.getIssueStatusName(), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_PRIORITY,
				before.getSettingCodeId(), after.getSettingCodeId(), before.getSettingName(), after.getSettingName(),
				reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_ASSIGNEE,
				before.getAssigneeId(), after.getAssigneeId(), before.getAssigneeName(), after.getAssigneeName(),
				reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_PROGRESS_RATE,
				before.getProgressRate(), after.getProgressRate(), formatPercent(before.getProgressRate()),
				formatPercent(after.getProgressRate()), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_ESTIMATED_HOURS,
				before.getEstimatedHours(), after.getEstimatedHours(), formatNumber(before.getEstimatedHours()),
				formatNumber(after.getEstimatedHours()), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_START_DATE,
				before.getStartDate(), after.getStartDate(), formatDate(before.getStartDate()),
				formatDate(after.getStartDate()), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_DUE_DATE,
				before.getDueDate(), after.getDueDate(), formatDate(before.getDueDate()), formatDate(after.getDueDate()),
				reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_MILESTONE,
				before.getMilestoneId(), after.getMilestoneId(), before.getMilestoneTitle(), after.getMilestoneTitle(),
				reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_PARENT_ISSUE,
				before.getParentIssueId(), after.getParentIssueId(), before.getParentIssueTitle(),
				after.getParentIssueTitle(), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_TITLE, before.getTitle(),
				after.getTitle(), before.getTitle(), after.getTitle(), reason);
		addUpdateHistory(historyList, historyGroupId, after.getIssueId(), userId, FIELD_DESCRIPTION,
				before.getDescription(), after.getDescription(), before.getDescription(), after.getDescription(),
				reason);

		for (IssuesVO history : historyList) {
			insertIssueHistory(history);
		}
	}

	private void addUpdateHistory(List<IssuesVO> historyList, String historyGroupId, String issueId, String changedBy,
			String fieldCode, Object beforeKey, Object afterKey, String beforeValue, String afterValue, String reason) {
		if (Objects.equals(beforeKey, afterKey)) {
			return;
		}

		IssuesVO history = new IssuesVO();
		history.setHistoryGroupId(historyGroupId);
		history.setIssueId(issueId);
		history.setChangedBy(changedBy);
		history.setChangeTypeCode(CHANGE_TYPE_UPDATE_CODE);
		history.setFieldCode(fieldCode);
		history.setFieldName(resolveFieldName(fieldCode));
		history.setReason(reason);
		history.setBeforeValue(emptyToDash(beforeValue));
		history.setAfterValue(emptyToDash(afterValue));
		historyList.add(history);
	}

	private String defaultHistoryReason(String reason, String defaultReason) {
		return isBlank(reason) ? defaultReason : reason;
	}

	private void insertIssueHistory(String historyGroupId, String issueId, String changedBy, String changeTypeCode,
			String fieldCode, String reason, String beforeValue, String afterValue) {
		IssuesVO history = new IssuesVO();
		history.setHistoryGroupId(historyGroupId);
		history.setIssueId(issueId);
		history.setChangedBy(changedBy);
		history.setChangeTypeCode(changeTypeCode);
		history.setFieldCode(fieldCode);
		history.setFieldName(resolveFieldName(fieldCode));
		history.setReason(defaultHistoryReason(reason, resolveChangeTypeDisplayName(changeTypeCode)));
		history.setBeforeValue(emptyToDash(beforeValue));
		history.setAfterValue(emptyToDash(afterValue));

		insertIssueHistory(history);
	}

	private void insertIssueHistory(IssuesVO history) {
		if (history == null) {
			return;
		}

		if (isBlank(history.getHistoryGroupId())) {
			history.setHistoryGroupId(newHistoryGroupId());
		}

		if (isBlank(history.getFieldName())) {
			history.setFieldName(resolveFieldName(history.getFieldCode()));
		}

		history.setReason(defaultHistoryReason(history.getReason(),
				resolveChangeTypeDisplayName(history.getChangeTypeCode())));
		history.setBeforeValue(emptyToDash(history.getBeforeValue()));
		history.setAfterValue(emptyToDash(history.getAfterValue()));

		issuesMapper.insertIssueHistoryByProcedure(history);
	}

	private Map<String, Object> buildDeletedIssueDetailPageData(IssuesVO projectInfo, String issueId,
			List<IssuesVO> historyRows) {
		IssuesVO deletedIssue = new IssuesVO();
		deletedIssue.setIssueId(issueId);
		deletedIssue.setProjectId(projectInfo.getProjectId());
		deletedIssue.setProjectName(projectInfo.getProjectName());
		deletedIssue.setOwnerId(projectInfo.getOwnerId());
		deletedIssue.setDisplayIssueNo(issueId);
		deletedIssue.setIssueTypeName("삭제된 일감");
		deletedIssue.setTitle("삭제된 일감입니다.");

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("issue", deletedIssue);
		pageData.put("fieldSettingList", Collections.emptyList());
		pageData.put("priorityList", Collections.emptyList());
		pageData.put("assigneeList", Collections.emptyList());
		pageData.put("milestoneList", Collections.emptyList());
		pageData.put("parentIssueList", Collections.emptyList());
		pageData.put("availableStatusList", Collections.emptyList());
		pageData.put("historyList", historyRows);
		pageData.put("historyGroupList", buildHistoryGroupList(historyRows));
		pageData.put("deletedIssue", true);
		pageData.put("deletedIssueMessage", "삭제된 일감입니다. 상세 데이터는 삭제되었지만 작업내역은 보존되어 있습니다.");
		pageData.put("canUpdateIssue", false);
		pageData.put("canDeleteIssue", false);
		return pageData;
	}

	private List<IssueHistoryGroupVO> buildHistoryGroupList(List<IssuesVO> historyRows) {
		if (historyRows == null || historyRows.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, IssueHistoryGroupVO> groupMap = new LinkedHashMap<>();

		for (IssuesVO history : historyRows) {
			String historyGroupId = trimToNull(history.getHistoryGroupId());

			if (historyGroupId == null) {
				historyGroupId = history.getHistoryId();
				history.setHistoryGroupId(historyGroupId);
			}

			history.setChangeTypeDisplayName(resolveChangeTypeDisplayName(history.getChangeTypeCode()));

			if (isBlank(history.getFieldName())) {
				history.setFieldName(resolveFieldName(history.getFieldCode()));
			}

			IssueHistoryGroupVO group = groupMap.get(historyGroupId);

			if (group == null) {
				group = new IssueHistoryGroupVO();
				group.setHistoryGroupId(historyGroupId);
				group.setChangedBy(history.getChangedBy());
				group.setChangedByName(history.getChangedByName());
				group.setChangeTypeCode(history.getChangeTypeCode());
				group.setChangeTypeDisplayName(history.getChangeTypeDisplayName());
				group.setReason(history.getReason());
				group.setChangedAt(history.getChangedAt());
				groupMap.put(historyGroupId, group);
			}

			group.getHistoryList().add(history);
		}

		return new ArrayList<>(groupMap.values());
	}

	private String resolveChangeTypeDisplayName(String changeTypeCode) {
		return CHANGE_TYPE_DISPLAY_NAME_MAP.getOrDefault(changeTypeCode, emptyToDash(changeTypeCode));
	}

	private String resolveFieldName(String fieldCode) {
		return FIELD_NAME_MAP.getOrDefault(fieldCode, emptyToDash(fieldCode));
	}

	private String newHistoryGroupId() {
		return UUID.randomUUID().toString();
	}

	private String displayIssueLabel(IssuesVO issue) {
		if (issue == null) {
			return "-";
		}

		String displayIssueNo = issue.getDisplayIssueNo();

		if (isBlank(displayIssueNo)) {
			displayIssueNo = formatDisplayIssueNo(issue.getIssueNo());
		}

		String title = trimToNull(issue.getTitle());

		if (isBlank(displayIssueNo)) {
			return emptyToDash(title);
		}

		return title == null ? displayIssueNo : displayIssueNo + " " + title;
	}

	private String formatDisplayIssueNo(Integer issueNo) {
		if (issueNo == null) {
			return null;
		}

		return "#" + String.format("%04d", issueNo);
	}

	private String formatPercent(Integer value) {
		return value == null ? null : value + "%";
	}

	private String formatNumber(Integer value) {
		return value == null ? null : String.valueOf(value);
	}

	private String formatDate(LocalDate value) {
		return value == null ? null : value.toString();
	}

	private void deletePhysicalFile(String filePath) {
		String checkedFilePath = trimToNull(filePath);

		if (checkedFilePath == null) {
			return;
		}

		File file = new File(checkedFilePath);

		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	private String emptyToDash(String value) {
		String trimmedValue = trimToNull(value);
		return trimmedValue == null ? "-" : trimmedValue;
	}

	private void validateIssueCreateSaveCondition(IssuesVO validation) {
		if (validation == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}

		if (!"Y".equals(validation.getPermissionYn())) {
			throw new IllegalArgumentException("일감 추가 권한이 없습니다.");
		}

		if (isBlank(validation.getInitialStatusId())) {
			throw new IllegalArgumentException("선택한 프로젝트에서 사용할 수 없는 일감 유형입니다.");
		}

		if (validation.getVersionCount() == null || validation.getVersionCount() == 0) {
			throw new IllegalArgumentException("선택한 버전은 현재 일감 생성에 사용할 수 없습니다.");
		}

		if (validation.getPriorityCount() == null || validation.getPriorityCount() == 0) {
			throw new IllegalArgumentException("선택한 우선순위를 사용할 수 없습니다.");
		}
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

	private IssuesVO validateProjectPermissionAccess(String projectId, String userId, String permissionCode,
			String permissionErrorMessage) {
		validateUserId(userId);

		if (isBlank(projectId)) {
			throw new IllegalArgumentException("프로젝트를 선택해주세요.");
		}

		IssuesVO project = issuesMapper.selectIssueCreateProjectAccess(projectId, userId, permissionCode);

		if (project == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}

		if (!"Y".equals(project.getPermissionYn())) {
			throw new IllegalArgumentException(permissionErrorMessage);
		}

		return project;
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

	private void validateIssueUpdatePermission(String projectId, String issueId, String userId) {
		validateUserId(userId);

		if (isBlank(projectId) || isBlank(issueId)) {
			throw new IllegalArgumentException("수정할 일감 정보가 없습니다.");
		}

		if (!hasIssueUpdatePermission(projectId, issueId, userId)) {
			throw new IllegalArgumentException("일감 수정 권한이 없습니다.");
		}
	}

	private boolean hasIssueUpdatePermission(String projectId, String issueId, String userId) {
		return issuesMapper.countIssueUpdatePermission(projectId, issueId, userId, PERMISSION_ISSUE_UPDATE_CODE,
				PERMISSION_ISSUE_UPDATE_OWN_CODE) > 0;
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

	private void validateDynamicFieldsForCreate(IssuesVO issue, IssuesVO validation) {
		if (!"Y".equals(validation.getAssigneeUseYn())) {
			issue.setAssigneeId(null);
		} else {
			if ("Y".equals(validation.getAssigneeRequiredYn()) && isBlank(issue.getAssigneeId())) {
				throw new IllegalArgumentException("담당자를 선택해주세요.");
			}

			if (!isBlank(issue.getAssigneeId())
					&& (validation.getAssigneeCount() == null || validation.getAssigneeCount() == 0)) {
				throw new IllegalArgumentException("선택한 담당자는 프로젝트 구성원이 아닙니다.");
			}
		}

		if (!"Y".equals(validation.getMilestoneUseYn())) {
			issue.setMilestoneId(null);
		} else {
			if ("Y".equals(validation.getMilestoneRequiredYn()) && isBlank(issue.getMilestoneId())) {
				throw new IllegalArgumentException("마일스톤을 선택해주세요.");
			}

			if (!isBlank(issue.getMilestoneId())
					&& (validation.getMilestoneCount() == null || validation.getMilestoneCount() == 0)) {
				throw new IllegalArgumentException("선택한 마일스톤은 선택한 버전에 속하지 않습니다.");
			}
		}

		if (!"Y".equals(validation.getParentIssueUseYn())) {
			issue.setParentIssueId(null);
		} else {
			if ("Y".equals(validation.getParentIssueRequiredYn()) && isBlank(issue.getParentIssueId())) {
				throw new IllegalArgumentException("상위 일감을 선택해주세요.");
			}

			if (!isBlank(issue.getParentIssueId())
					&& (validation.getParentIssueCount() == null || validation.getParentIssueCount() == 0)) {
				throw new IllegalArgumentException("선택한 상위 일감을 사용할 수 없습니다.");
			}
		}

		if (!"Y".equals(validation.getStartDateUseYn())) {
			issue.setStartDate(null);
		} else if ("Y".equals(validation.getStartDateRequiredYn()) && issue.getStartDate() == null) {
			throw new IllegalArgumentException("시작일을 입력해주세요.");
		}

		if (!"Y".equals(validation.getDueDateUseYn())) {
			issue.setDueDate(null);
		} else if ("Y".equals(validation.getDueDateRequiredYn()) && issue.getDueDate() == null) {
			throw new IllegalArgumentException("완료일을 입력해주세요.");
		}

		if (issue.getStartDate() != null && issue.getDueDate() != null
				&& issue.getDueDate().isBefore(issue.getStartDate())) {
			throw new IllegalArgumentException("완료일은 시작일보다 빠를 수 없습니다.");
		}

		if (!"Y".equals(validation.getEstimatedHoursUseYn())) {
			issue.setEstimatedHours(null);
			return;
		}

		if ("Y".equals(validation.getEstimatedHoursRequiredYn()) && issue.getEstimatedHours() == null) {
			throw new IllegalArgumentException("추정시간을 입력해주세요.");
		}

		if (issue.getEstimatedHours() == null) {
			issue.setEstimatedHours(0);
		}

		if (issue.getEstimatedHours() < 0 || issue.getEstimatedHours() > 99999) {
			throw new IllegalArgumentException("추정시간은 0부터 99999 사이로 입력해주세요.");
		}
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
		searchVO.setMilestoneId(trimToNull(searchVO.getMilestoneId()));
		searchVO.setIssueStatusId(trimToNull(searchVO.getIssueStatusId()));
		searchVO.setSettingCodeId(trimToNull(searchVO.getSettingCodeId()));
		searchVO.setAssigneeId(trimToNull(searchVO.getAssigneeId()));
		searchVO.setProgressRange(trimToNull(searchVO.getProgressRange()));
		searchVO.setClosedYn(normalizeClosedYn(searchVO.getClosedYn()));
		searchVO.setIssueTypeIdList(normalizeSearchList(searchVO.getIssueTypeIdList(), searchVO.getIssueTypeId()));
		searchVO.setVersionIdList(normalizeSearchList(searchVO.getVersionIdList(), searchVO.getVersionId()));
		searchVO.setMilestoneIdList(normalizeSearchList(searchVO.getMilestoneIdList(), searchVO.getMilestoneId()));
		searchVO.setIssueStatusIdList(
				normalizeSearchList(searchVO.getIssueStatusIdList(), searchVO.getIssueStatusId()));
		searchVO.setSettingCodeIdList(
				normalizeSearchList(searchVO.getSettingCodeIdList(), searchVO.getSettingCodeId()));
		searchVO.setAssigneeIdList(normalizeSearchList(searchVO.getAssigneeIdList(), searchVO.getAssigneeId()));
		searchVO.setProgressRangeList(
				normalizeProgressRangeList(searchVO.getProgressRangeList(), searchVO.getProgressRange()));
		searchVO.setIssueNoSort(normalizeIssueNoSort(searchVO.getIssueNoSort()));
		validateSearchDateRange(searchVO);
	}

	private String normalizeClosedYn(String closedYn) {
		String checkedClosedYn = trimToNull(closedYn);

		if (checkedClosedYn == null) {
			return null;
		}

		if ("Y".equals(checkedClosedYn) || "N".equals(checkedClosedYn)) {
			return checkedClosedYn;
		}

		throw new IllegalArgumentException("완료여부 검색 조건이 올바르지 않습니다.");
	}

	private void validateSearchDateRange(IssuesVO searchVO) {
		if (searchVO.getStartDate() != null && searchVO.getDueDate() != null
				&& searchVO.getDueDate().isBefore(searchVO.getStartDate())) {
			throw new IllegalArgumentException("완료기한은 시작일보다 빠를 수 없습니다.");
		}
	}

	private List<String> normalizeSearchList(List<String> valueList, String legacyValue) {
		List<String> normalizedList = valueList == null ? Collections.emptyList()
				: valueList.stream().map(this::trimToNull).filter(value -> value != null).distinct()
						.collect(Collectors.toList());

		if (!normalizedList.isEmpty()) {
			return normalizedList;
		}

		String checkedLegacyValue = trimToNull(legacyValue);

		if (checkedLegacyValue == null) {
			return Collections.emptyList();
		}

		return Collections.singletonList(checkedLegacyValue);
	}

	private List<String> normalizeProgressRangeList(List<String> valueList, String legacyValue) {
		List<String> allowedRangeList = Arrays.asList("0-20", "21-40", "41-60", "61-80", "81-100");
		List<String> normalizedList = normalizeSearchList(valueList, legacyValue).stream()
				.filter(allowedRangeList::contains).collect(Collectors.toList());

		if (normalizedList.isEmpty() && !isBlank(legacyValue)) {
			throw new IllegalArgumentException("진척도 검색 범위가 올바르지 않습니다.");
		}

		return normalizedList;
	}

	private void applyProgressRange(IssuesVO searchVO) {
		searchVO.setProgressMin(null);
		searchVO.setProgressMax(null);

		if (searchVO.getProgressRange() == null) {
			return;
		}

		String[] progressValues = searchVO.getProgressRange().split("-");

		if (progressValues.length != 2) {
			throw new IllegalArgumentException("진척도 검색 범위가 올바르지 않습니다.");
		}

		try {
			int progressMin = Integer.parseInt(progressValues[0]);
			int progressMax = Integer.parseInt(progressValues[1]);

			if (progressMin < 0 || progressMax > 100 || progressMin > progressMax) {
				throw new IllegalArgumentException("진척도 검색 범위가 올바르지 않습니다.");
			}

			searchVO.setProgressMin(progressMin);
			searchVO.setProgressMax(progressMax);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("진척도 검색 범위가 올바르지 않습니다.");
		}
	}

	private void applyPaging(IssuesVO searchVO) {
		if (searchVO.getPage() == null || searchVO.getPage() < 1) {
			searchVO.setPage(1);
		}

		searchVO.setPageSize(ISSUE_PAGE_SIZE);
		setPageProbeRange(searchVO);
	}

	private void setPageProbeRange(IssuesVO searchVO) {
		int page = searchVO.getPage();
		int pageSize = searchVO.getPageSize();
		int blockStartPage = getIssuePageBlockStartPage(page);
		int blockEndPage = blockStartPage + ISSUE_PAGE_BLOCK_SIZE - 1;

		searchVO.setStartRow(getIssuePageStartRow(blockStartPage));
		searchVO.setEndRow(blockEndPage * pageSize + 1);
	}

	private void applyPageProbeResult(IssuesVO searchVO, List<IssuesVO> pageBlockRows, int currentStartRow,
			int currentEndRow) {
		searchVO.setHasPreviousPage(searchVO.getPage() > 1);
		searchVO.setHasNextPage(hasIssueRowAtOrAfter(pageBlockRows, currentEndRow));
		searchVO.setPageList(buildIssuePageList(searchVO.getPage(), pageBlockRows));
	}

	private boolean hasIssueRowAtOrAfter(List<IssuesVO> pageBlockRows, int rowNo) {
		return pageBlockRows.stream().map(IssuesVO::getRowNo).filter(Objects::nonNull).anyMatch(no -> no >= rowNo);
	}

	private List<Integer> buildIssuePageList(int currentPage, List<IssuesVO> pageBlockRows) {
		int blockStartPage = getIssuePageBlockStartPage(currentPage);
		int blockEndPage = blockStartPage + ISSUE_PAGE_BLOCK_SIZE - 1;
		int blockStartRow = getIssuePageStartRow(blockStartPage);
		int blockLastDisplayRow = blockEndPage * ISSUE_PAGE_SIZE;
		int maxDisplayRow = pageBlockRows.stream().map(IssuesVO::getRowNo).filter(Objects::nonNull)
				.filter(rowNo -> rowNo <= blockLastDisplayRow).max(Integer::compareTo).orElse(0);
		int pageCountInBlock = maxDisplayRow < blockStartRow ? 0
				: (int) Math.ceil((double) (maxDisplayRow - blockStartRow + 1) / ISSUE_PAGE_SIZE);
		List<Integer> pageList = new ArrayList<>();

		for (int i = 0; i < pageCountInBlock; i++) {
			int pageNo = blockStartPage + i;
			if (pageNo > blockEndPage) {
				break;
			}
			pageList.add(pageNo);
		}

		if (pageList.isEmpty() || !pageList.contains(currentPage)) {
			pageList.add(currentPage);
		}

		return pageList;
	}

	private int getIssuePageBlockStartPage(int page) {
		return ((page - 1) / ISSUE_PAGE_BLOCK_SIZE) * ISSUE_PAGE_BLOCK_SIZE + 1;
	}

	private int getIssuePageStartRow(int page) {
		return (page - 1) * ISSUE_PAGE_SIZE + 1;
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
