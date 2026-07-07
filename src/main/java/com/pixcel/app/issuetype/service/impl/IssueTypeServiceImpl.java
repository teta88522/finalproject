package com.pixcel.app.issuetype.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.issuetype.mapper.IssueTypeMapper;
import com.pixcel.app.issuetype.service.IssueTypeService;
import com.pixcel.app.issuetype.service.IssueTypeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueTypeServiceImpl implements IssueTypeService {

	private final IssueTypeMapper issueTypeMapper;

	private static final List<String> FIELD_CODE_LIST = Arrays.asList("ASSIGNEE", "MILESTONE", "PARENT_ISSUE",
			"START_DATE", "DUE_DATE", "ESTIMATED_HOURS");

	private static final String OPTION_ISSUE_TYPE = "ISSUE_TYPE";
	private static final String OPTION_ISSUE_STATUS = "ISSUE_STATUS";
	private static final String OPTION_PROJECT = "PROJECT";

	@Override
	public Map<String, Object> getIssueTypeListPageData(IssueTypeVO searchVO) {
		validateUserId(searchVO.getUserId());
		normalizeSearchCondition(searchVO);

		List<IssueTypeVO> pageRows = issueTypeMapper.selectIssueTypeListPageRows(searchVO);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("issueTypeList", filterOptionRows(pageRows, OPTION_ISSUE_TYPE));
		pageData.put("issueStatusList", filterOptionRows(pageRows, OPTION_ISSUE_STATUS));

		return pageData;
	}

	@Override
	public Map<String, Object> getIssueTypeFormPageData(String userId) {
		validateUserId(userId);

		List<IssueTypeVO> optionRows = issueTypeMapper.selectIssueTypeFormOptionRows(userId);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("issueStatusList", filterOptionRows(optionRows, OPTION_ISSUE_STATUS));
		pageData.put("projectList", filterOptionRows(optionRows, OPTION_PROJECT));

		return pageData;
	}

	private List<IssueTypeVO> filterOptionRows(List<IssueTypeVO> rows, String optionGroup) {
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}

		return rows.stream()
				.filter(row -> optionGroup.equals(row.getOptionGroup()))
				.collect(Collectors.toList());
	}

	// 검색조건에 맞는 사용자별 일감유형 목록을 조회한다.
	@Override
	public List<IssueTypeVO> getIssueTypeList(IssueTypeVO searchVO) {

		validateUserId(searchVO.getUserId());
		normalizeSearchCondition(searchVO);

		return issueTypeMapper.selectIssueTypeList(searchVO);
	}

	private void normalizeSearchCondition(IssueTypeVO searchVO) {
		searchVO.setInitialStatusIdList(normalizeSearchList(searchVO.getInitialStatusIdList(),
				searchVO.getInitialStatusId()));
	}

	private List<String> normalizeSearchList(List<String> valueList, String legacyValue) {
		List<String> normalizedList = valueList == null ? Collections.emptyList() : valueList.stream()
				.map(this::trimToNull)
				.filter(value -> value != null)
				.distinct()
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

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isEmpty() ? null : trimmedValue;
	}

	private List<String> normalizeProjectIdList(List<String> projectIdList) {
		if (projectIdList == null || projectIdList.isEmpty()) {
			return Collections.emptyList();
		}

		return projectIdList.stream()
				.map(this::trimToNull)
				.filter(projectId -> projectId != null)
				.distinct()
				.collect(Collectors.toList());
	}

	// 일감유형별 표준 항목 사용 여부 요약 목록을 조회한다.
	@Override
	public List<IssueTypeVO> getIssueTypeSummaryList(IssueTypeVO searchVO) {

		validateUserId(searchVO.getUserId());

		return issueTypeMapper.selectIssueTypeSummaryList(searchVO);
	}

	// 사용자별 일감유형 상세 정보를 조회한다.
	@Override
	public IssueTypeVO getIssueTypeDetail(String issueTypeId, String userId) {
		validateUserId(userId);

		IssueTypeVO issueType = issueTypeMapper.selectIssueTypeDetail(issueTypeId, userId);

		if (issueType == null) {
			throw new IllegalArgumentException("존재하지 않는 일감유형입니다.");
		}

		issueType.setProjectIdList(issueTypeMapper.selectAppliedProjectIdList(issueTypeId, userId));
		issueType.setUsedProjectIdList(issueTypeMapper.selectIssueTypeUsedProjectIdList(issueTypeId, userId));
		applyFieldSettingList(issueType, issueTypeMapper.selectFieldSettingListByIssueType(issueTypeId, userId));

		return issueType;
	}

	private void applyFieldSettingList(IssueTypeVO issueType, List<IssueTypeVO> fieldSettingList) {
		List<String> usedFieldCodeList = new ArrayList<>();
		List<String> requiredFieldCodeList = new ArrayList<>();

		for (IssueTypeVO fieldSetting : fieldSettingList) {
			if ("Y".equals(fieldSetting.getUseYn())) {
				usedFieldCodeList.add(fieldSetting.getFieldCode());
			}

			if ("Y".equals(fieldSetting.getRequiredYn())) {
				requiredFieldCodeList.add(fieldSetting.getFieldCode());
			}
		}

		issueType.setFieldCodeList(usedFieldCodeList);
		issueType.setRequiredFieldCodeList(requiredFieldCodeList);
	}

	// 현재 사용자가 소유한 프로젝트 목록을 조회한다.
	@Override
	public List<IssueTypeVO> getProjectList(String userId) {
		validateUserId(userId);

		return issueTypeMapper.selectProjectList(userId);
	}

	// 신규 일감유형을 등록한다.
	@Override
	@Transactional
	public void createIssueType(IssueTypeVO issueType, String userId) {
		validateUserId(userId);

		issueType.setUserId(userId);
		issueType.setProjectIdList(normalizeProjectIdList(issueType.getProjectIdList()));

		validateIssueType(issueType);
		validateFieldSetting(issueType);
		validateIssueTypeSaveCondition(issueType);

		issueTypeMapper.insertIssueType(issueType);

		insertIssueTypeProjectListBulk(issueType);
		insertIssueTypeFieldSettingListBulk(issueType);
	}

	// 기존 일감유형을 복사하여 신규 일감유형으로 등록한다.
	@Override
	@Transactional
	public void updateIssueType(IssueTypeVO issueType, String userId) {
		validateUserId(userId);

		if (issueType == null || trimToNull(issueType.getIssueTypeId()) == null) {
			throw new IllegalArgumentException("수정할 일감유형 정보가 없습니다.");
		}

		issueType.setUserId(userId);
		issueType.setIssueTypeId(trimToNull(issueType.getIssueTypeId()));
		issueType.setProjectIdList(normalizeProjectIdList(issueType.getProjectIdList()));

		validateIssueType(issueType);
		validateFieldSetting(issueType);
		validateIssueTypeUpdateCondition(issueType);

		int updateCount = issueTypeMapper.updateIssueType(issueType);
		if (updateCount == 0) {
			throw new IllegalArgumentException("수정할 일감유형이 없습니다.");
		}

		boolean issueTypeUsedByIssue = isIssueTypeUsedByIssue(issueType.getIssueTypeId(), userId);
		updateIssueTypeProjectList(issueType, userId);
		if (!issueTypeUsedByIssue) {
			issueTypeMapper.deleteIssueTypeFieldSettingByIssueTypeId(issueType.getIssueTypeId(), userId);
			insertIssueTypeFieldSettingListBulk(issueType);
		}
	}

	@Override
	@Transactional
	public void copyIssueType(IssueTypeVO issueType, String userId) {
		validateUserId(userId);

		issueType.setUserId(userId);
		issueType.setIssueTypeId(null);
		issueType.setProjectIdList(normalizeProjectIdList(issueType.getProjectIdList()));

		validateIssueType(issueType);
		validateFieldSetting(issueType);
		validateIssueTypeSaveCondition(issueType);

		issueTypeMapper.insertIssueType(issueType);

		insertIssueTypeProjectListBulk(issueType);
		insertIssueTypeFieldSettingListBulk(issueType);
	}

	// 사용 중이지 않은 일감유형을 삭제한다.
	@Override
	@Transactional
	public void removeIssueType(String issueTypeId, String userId) {
		validateUserId(userId);

		IssueTypeVO issueType = issueTypeMapper.selectIssueTypeDeleteCheck(issueTypeId, userId);

		if (issueType == null) {
			throw new IllegalArgumentException("존재하지 않는 일감유형입니다.");
		}

		if (issueType.getUsedCount() != null && issueType.getUsedCount() > 0) {
			throw new IllegalArgumentException("이미 일감 또는 업무흐름에서 사용 중인 일감유형이므로 삭제할 수 없습니다.");
		}

		issueTypeMapper.deleteIssueTypeProjectByIssueTypeId(issueTypeId, userId);
		issueTypeMapper.deleteIssueTypeFieldSettingByIssueTypeId(issueTypeId, userId);
		issueTypeMapper.deleteIssueType(issueTypeId, userId);
	}

	private void validateIssueTypeSaveCondition(IssueTypeVO issueType) {
		IssueTypeVO validation = issueTypeMapper.selectIssueTypeSaveValidation(issueType);

		if (validation == null || validation.getInitialStatusCount() == null
				|| validation.getInitialStatusCount() == 0) {
			throw new IllegalArgumentException("선택한 초기 상태를 사용할 수 없습니다.");
		}

		if (validation.getDuplicateCount() != null && validation.getDuplicateCount() > 0) {
			throw new IllegalArgumentException("이미 사용 중인 일감유형명입니다.");
		}

		List<String> projectIdList = issueType.getProjectIdList();

		if (projectIdList != null && !projectIdList.isEmpty()) {
			Set<String> uniqueProjectIdSet = new HashSet<>(projectIdList);
			int selectedProjectCount = validation.getSelectedProjectCount() == null ? 0
					: validation.getSelectedProjectCount();

			if (selectedProjectCount != uniqueProjectIdSet.size()) {
				throw new IllegalArgumentException("선택한 프로젝트 중 사용할 수 없는 프로젝트가 있습니다.");
			}
		}
	}

	// 일감유형 적용 프로젝트를 등록한다.
	private void validateIssueTypeUpdateCondition(IssueTypeVO issueType) {
		IssueTypeVO validation = issueTypeMapper.selectIssueTypeUpdateValidation(issueType);

		if (validation == null || validation.getInitialStatusCount() == null
				|| validation.getInitialStatusCount() == 0) {
			throw new IllegalArgumentException("선택한 초기 상태를 사용할 수 없습니다.");
		}

		if (validation.getDuplicateCount() != null && validation.getDuplicateCount() > 0) {
			throw new IllegalArgumentException("이미 사용 중인 일감유형명입니다.");
		}

		List<String> projectIdList = issueType.getProjectIdList();

		if (projectIdList != null && !projectIdList.isEmpty()) {
			Set<String> uniqueProjectIdSet = new HashSet<>(projectIdList);
			int selectedProjectCount = validation.getSelectedProjectCount() == null ? 0
					: validation.getSelectedProjectCount();

			if (selectedProjectCount != uniqueProjectIdSet.size()) {
				throw new IllegalArgumentException("선택한 프로젝트 중 사용할 수 없는 프로젝트가 있습니다.");
			}
		}
	}

	private void updateIssueTypeProjectList(IssueTypeVO issueType, String userId) {
		List<String> currentProjectIdList = issueTypeMapper.selectAppliedProjectIdList(issueType.getIssueTypeId(),
				userId);
		if (currentProjectIdList == null) {
			currentProjectIdList = Collections.emptyList();
		}

		List<String> selectedProjectIdList = issueType.getProjectIdList() == null ? Collections.emptyList()
				: issueType.getProjectIdList();
		Set<String> selectedProjectIdSet = new HashSet<>(selectedProjectIdList);

		List<String> removedProjectIdList = currentProjectIdList.stream()
				.filter(projectId -> !selectedProjectIdSet.contains(projectId))
				.collect(Collectors.toList());

		if (!removedProjectIdList.isEmpty()) {
			int usedCount = issueTypeMapper.countIssueTypeUsedInProjects(issueType.getIssueTypeId(), userId,
					removedProjectIdList);

			if (usedCount > 0) {
				throw new IllegalArgumentException("이미 일감에서 사용 중인 프로젝트에서는 이 일감유형을 해제할 수 없습니다.");
			}

			issueTypeMapper.deleteIssueTypeProjectByProjectIdList(issueType.getIssueTypeId(), userId,
					removedProjectIdList);
		}

		Set<String> currentProjectIdSet = new HashSet<>(currentProjectIdList);
		List<String> addedProjectIdList = selectedProjectIdList.stream()
				.filter(projectId -> !currentProjectIdSet.contains(projectId))
				.collect(Collectors.toList());

		if (addedProjectIdList.isEmpty()) {
			return;
		}

		IssueTypeVO insertTarget = new IssueTypeVO();
		insertTarget.setIssueTypeId(issueType.getIssueTypeId());
		insertTarget.setProjectIdList(addedProjectIdList);
		insertIssueTypeProjectListBulk(insertTarget);
	}

	private boolean isIssueTypeUsedByIssue(String issueTypeId, String userId) {
		List<String> usedProjectIdList = issueTypeMapper.selectIssueTypeUsedProjectIdList(issueTypeId, userId);
		return usedProjectIdList != null && !usedProjectIdList.isEmpty();
	}

	private void insertIssueTypeProjectList(IssueTypeVO issueType) {

		if (issueType.getProjectIdList() == null || issueType.getProjectIdList().isEmpty()) {
			return;
		}

		for (String projectId : issueType.getProjectIdList()) {
			IssueTypeVO project = new IssueTypeVO();
			project.setIssueTypeId(issueType.getIssueTypeId());
			project.setProjectId(projectId);

			issueTypeMapper.insertIssueTypeProject(project);
		}
	}

	private void insertIssueTypeProjectListBulk(IssueTypeVO issueType) {
		if (issueType.getProjectIdList() == null || issueType.getProjectIdList().isEmpty()) {
			return;
		}

		issueTypeMapper.insertIssueTypeProjectList(issueType);
	}

	// 일감유형 항목 설정을 등록한다.
	private void insertIssueTypeFieldSettingList(IssueTypeVO issueType) {

		Set<String> usedFieldSet = new HashSet<>(issueType.getFieldCodeList());
		Set<String> requiredFieldSet = new HashSet<>(issueType.getRequiredFieldCodeList());

		for (String fieldCode : FIELD_CODE_LIST) {
			IssueTypeVO fieldSetting = new IssueTypeVO();
			fieldSetting.setIssueTypeId(issueType.getIssueTypeId());
			fieldSetting.setFieldCode(fieldCode);
			fieldSetting.setUseYn(usedFieldSet.contains(fieldCode) ? "Y" : "N");
			fieldSetting.setRequiredYn(requiredFieldSet.contains(fieldCode) ? "Y" : "N");

			issueTypeMapper.insertIssueTypeFieldSetting(fieldSetting);
		}
	}

	private void insertIssueTypeFieldSettingListBulk(IssueTypeVO issueType) {
		Set<String> usedFieldSet = new HashSet<>(issueType.getFieldCodeList());
		Set<String> requiredFieldSet = new HashSet<>(issueType.getRequiredFieldCodeList());
		List<IssueTypeVO> fieldSettingList = new ArrayList<>();
		int sortNo = 1;

		for (String fieldCode : FIELD_CODE_LIST) {
			IssueTypeVO fieldSetting = new IssueTypeVO();
			fieldSetting.setIssueTypeId(issueType.getIssueTypeId());
			fieldSetting.setFieldCode(fieldCode);
			fieldSetting.setUseYn(usedFieldSet.contains(fieldCode) ? "Y" : "N");
			fieldSetting.setRequiredYn(requiredFieldSet.contains(fieldCode) ? "Y" : "N");
			fieldSetting.setSortNo(sortNo++);
			fieldSettingList.add(fieldSetting);
		}

		issueType.setFieldSettingList(fieldSettingList);
		issueTypeMapper.insertIssueTypeFieldSettingList(issueType);
	}

	// 로그인 사용자 ID 값을 검증한다.
	private void validateUserId(String userId) {

		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("사용자 정보가 없습니다.");
		}
	}

	// 일감유형 기본 정보를 검증한다.
	private void validateIssueType(IssueTypeVO issueType) {

		if (issueType.getIssueTypeName() == null || issueType.getIssueTypeName().trim().isEmpty()) {
			throw new IllegalArgumentException("일감유형명을 입력하세요.");
		}

		if (issueType.getInitialStatusId() == null || issueType.getInitialStatusId().trim().isEmpty()) {
			throw new IllegalArgumentException("초기 상태를 선택하세요.");
		}

		if (issueType.getFieldCodeList() == null) {
			issueType.setFieldCodeList(List.of());
		}

		if (issueType.getRequiredFieldCodeList() == null) {
			issueType.setRequiredFieldCodeList(List.of());
		}
	}

	// 선택한 초기 상태가 현재 사용자의 상태인지 검증한다.
	private void validateInitialStatusOwner(String initialStatusId, String userId) {

		int count = issueTypeMapper.countInitialStatusByUser(initialStatusId, userId);

		if (count == 0) {
			throw new IllegalArgumentException("선택한 초기 상태를 사용할 수 없습니다.");
		}
	}

	// 선택한 프로젝트들이 현재 사용자의 프로젝트인지 검증한다.
	private void validateProjectOwner(List<String> projectIdList, String userId) {

		if (projectIdList == null || projectIdList.isEmpty()) {
			return;
		}

		Set<String> uniqueProjectIdSet = new HashSet<>(projectIdList);

		int selectedProjectCount = issueTypeMapper.countSelectedProjectByUser(userId, projectIdList);

		if (selectedProjectCount != uniqueProjectIdSet.size()) {
			throw new IllegalArgumentException("선택한 프로젝트 중 사용할 수 없는 프로젝트가 있습니다.");
		}
	}

	// 항목 사용/필수 설정 값을 검증한다.
	private void validateFieldSetting(IssueTypeVO issueType) {

		Set<String> usedFieldSet = new HashSet<>(issueType.getFieldCodeList());
		Set<String> requiredFieldSet = new HashSet<>(issueType.getRequiredFieldCodeList());

		for (String fieldCode : usedFieldSet) {
			if (!FIELD_CODE_LIST.contains(fieldCode)) {
				throw new IllegalArgumentException("사용할 수 없는 항목 설정입니다.");
			}
		}

		for (String fieldCode : requiredFieldSet) {
			if (!FIELD_CODE_LIST.contains(fieldCode)) {
				throw new IllegalArgumentException("사용할 수 없는 필수 항목 설정입니다.");
			}

			if (!usedFieldSet.contains(fieldCode)) {
				throw new IllegalArgumentException("필수 항목은 먼저 사용 항목으로 선택해야 합니다.");
			}
		}
	}

	// 사용자별 일감유형명 중복 여부를 검증한다.
	private void validateDuplicateIssueTypeName(String userId, String issueTypeName) {

		int duplicateCount = issueTypeMapper.countDuplicateIssueTypeName(userId, issueTypeName);

		if (duplicateCount > 0) {
			throw new IllegalArgumentException("이미 사용 중인 일감유형명입니다.");
		}
	}
}
