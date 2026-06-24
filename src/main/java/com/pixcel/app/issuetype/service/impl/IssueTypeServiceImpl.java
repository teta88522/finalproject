package com.pixcel.app.issuetype.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	// 검색조건에 맞는 사용자별 일감유형 목록을 조회한다.
	@Override
	public List<IssueTypeVO> getIssueTypeList(IssueTypeVO searchVO) {

		validateUserId(searchVO.getUserId());

		return issueTypeMapper.selectIssueTypeList(searchVO);
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
		issueType.setFieldCodeList(issueTypeMapper.selectUsedFieldCodeList(issueTypeId, userId));
		issueType.setRequiredFieldCodeList(issueTypeMapper.selectRequiredFieldCodeList(issueTypeId, userId));

		return issueType;
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
		issueType.setRoadmapYn(null);

		validateIssueType(issueType);
		validateInitialStatusOwner(issueType.getInitialStatusId(), userId);
		validateProjectOwner(issueType.getProjectIdList(), userId);
		validateFieldSetting(issueType);

		validateDuplicateIssueTypeName(userId, issueType.getIssueTypeName());

		issueTypeMapper.insertIssueType(issueType);

		insertIssueTypeProjectList(issueType);
		insertIssueTypeFieldSettingList(issueType);
	}

	// 기존 일감유형을 복사하여 신규 일감유형으로 등록한다.
	@Override
	@Transactional
	public void copyIssueType(IssueTypeVO issueType, String userId) {
		validateUserId(userId);

		issueType.setUserId(userId);
		issueType.setIssueTypeId(null);
		issueType.setRoadmapYn(null);

		validateIssueType(issueType);
		validateInitialStatusOwner(issueType.getInitialStatusId(), userId);
		validateProjectOwner(issueType.getProjectIdList(), userId);
		validateFieldSetting(issueType);

		validateDuplicateIssueTypeName(userId, issueType.getIssueTypeName());

		issueTypeMapper.insertIssueType(issueType);

		insertIssueTypeProjectList(issueType);
		insertIssueTypeFieldSettingList(issueType);
	}

	// 사용 중이지 않은 일감유형을 삭제한다.
	@Override
	@Transactional
	public void removeIssueType(String issueTypeId, String userId) {
		validateUserId(userId);

		IssueTypeVO issueType = issueTypeMapper.selectIssueTypeDetail(issueTypeId, userId);

		if (issueType == null) {
			throw new IllegalArgumentException("존재하지 않는 일감유형입니다.");
		}

		int usedCount = issueTypeMapper.countUsedIssueType(issueTypeId);

		if (usedCount > 0) {
			throw new IllegalArgumentException("이미 일감 또는 업무흐름에서 사용 중인 일감유형이므로 삭제할 수 없습니다.");
		}

		issueTypeMapper.deleteIssueTypeProjectByIssueTypeId(issueTypeId, userId);
		issueTypeMapper.deleteIssueTypeFieldSettingByIssueTypeId(issueTypeId, userId);
		issueTypeMapper.deleteIssueType(issueTypeId, userId);
	}

	// 일감유형 적용 프로젝트를 등록한다.
	private void insertIssueTypeProjectList(IssueTypeVO issueType) {

		for (String projectId : issueType.getProjectIdList()) {
			IssueTypeVO project = new IssueTypeVO();
			project.setIssueTypeId(issueType.getIssueTypeId());
			project.setProjectId(projectId);

			issueTypeMapper.insertIssueTypeProject(project);
		}
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

		if (issueType.getProjectIdList() == null || issueType.getProjectIdList().isEmpty()) {
			throw new IllegalArgumentException("적용 프로젝트를 1개 이상 선택하세요.");
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