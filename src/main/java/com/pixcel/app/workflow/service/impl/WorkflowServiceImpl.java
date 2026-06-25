package com.pixcel.app.workflow.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.workflow.mapper.WorkflowMapper;
import com.pixcel.app.workflow.service.WorkflowService;
import com.pixcel.app.workflow.service.WorkflowVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

	private final WorkflowMapper workflowMapper;

	// j001: 담당자, j002: 작성자, j003: 기본
	private static final List<String> APPLY_TARGET_CODE_LIST = Arrays.asList("j001", "j002", "j003");

	// 현재 사용자가 소유한 일감유형 목록을 조회한다.
	@Override
	public List<WorkflowVO> getIssueTypeList(String userId) {

		validateUserId(userId);

		return workflowMapper.selectIssueTypeList(userId);
	}

	// 현재 관리자가 생성한 역할 목록을 조회한다.
	@Override
	public List<WorkflowVO> getRoleList(String userId) {

		validateUserId(userId);

		return workflowMapper.selectRoleList(userId);
	}

	// 현재 사용자가 소유한 일감상태 목록을 조회한다.
	@Override
	public List<WorkflowVO> getIssueStatusList(WorkflowVO searchVO) {

		validateUserId(searchVO.getUserId());

		return workflowMapper.selectIssueStatusList(searchVO);
	}

	// 선택한 조건에 저장된 상태전환 키 목록을 조회한다.
	@Override
	public List<String> getSavedTransitionKeyList(WorkflowVO workflowVO) {

		validateUserId(workflowVO.getUserId());
		validateRequired(workflowVO.getIssueTypeId(), "일감유형");
		validateRequired(workflowVO.getRoleId(), "역할");
		validateApplyTargetCode(workflowVO.getApplyTargetCode());

		return workflowMapper.selectSavedTransitionKeyList(workflowVO);
	}

	// 상태전환 설정을 저장한다.
	@Transactional
	@Override
	public void saveWorkflowTransition(WorkflowVO workflowVO, String userId) {

		validateUserId(userId);

		workflowVO.setUserId(userId);
		workflowVO.setCreatedBy(userId);

		validateRequired(workflowVO.getIssueTypeId(), "일감유형");
		validateRequired(workflowVO.getRoleId(), "역할");
		validateApplyTargetCode(workflowVO.getApplyTargetCode());

		validateIssueTypeOwner(workflowVO.getIssueTypeId(), userId);
		validateRoleOwner(workflowVO.getRoleId(), userId);

		List<WorkflowVO> transitionList = parseTransitionKeyList(workflowVO);
		validateStatusOwner(transitionList, userId);

		workflowMapper.deleteWorkflowTransition(workflowVO);

		for (WorkflowVO transition : transitionList) {
			transition.setIssueTypeId(workflowVO.getIssueTypeId());
			transition.setRoleId(workflowVO.getRoleId());
			transition.setApplyTargetCode(workflowVO.getApplyTargetCode());
			transition.setCreatedBy(userId);

			workflowMapper.insertWorkflowTransition(transition);
		}
	}

	// 일감유형 소유자 검증
	private void validateIssueTypeOwner(String issueTypeId, String userId) {

		int count = workflowMapper.countIssueTypeByUser(issueTypeId, userId);

		if (count == 0) {
			throw new IllegalArgumentException("현재 사용자의 일감유형이 아닙니다.");
		}
	}

	// 역할 소유자 검증
	private void validateRoleOwner(String roleId, String userId) {

		int count = workflowMapper.countRole(roleId, userId);

		if (count == 0) {
			throw new IllegalArgumentException("현재 사용자의 역할이 아닙니다.");
		}
	}

	// 상태 소유자 검증
	private void validateStatusOwner(List<WorkflowVO> transitionList, String userId) {

		if (transitionList == null || transitionList.isEmpty()) {
			return;
		}

		Set<String> statusIdSet = new LinkedHashSet<>();

		for (WorkflowVO transition : transitionList) {
			statusIdSet.add(transition.getFromStatusId());
			statusIdSet.add(transition.getToStatusId());
		}

		List<String> statusIdList = new ArrayList<>(statusIdSet);

		int count = workflowMapper.countStatusByUser(userId, statusIdList);

		if (count != statusIdList.size()) {
			throw new IllegalArgumentException("현재 사용자의 일감상태가 아닌 값이 포함되어 있습니다.");
		}
	}

	// 체크박스 전송값을 fromStatusId / toStatusId로 변환
	private List<WorkflowVO> parseTransitionKeyList(WorkflowVO workflowVO) {

		List<WorkflowVO> transitionList = new ArrayList<>();

		List<String> transitionKeyList = workflowVO.getTransitionKeyList();

		if (transitionKeyList == null || transitionKeyList.isEmpty()) {
			return transitionList;
		}

		for (String transitionKey : transitionKeyList) {

			validateRequired(transitionKey, "상태전환 값");

			String[] statusIds = transitionKey.split("\\|");

			if (statusIds.length != 2) {
				throw new IllegalArgumentException("상태전환 값 형식이 올바르지 않습니다.");
			}

			String fromStatusId = statusIds[0];
			String toStatusId = statusIds[1];

			validateRequired(fromStatusId, "현재 상태");
			validateRequired(toStatusId, "다음 상태");

			if (fromStatusId.equals(toStatusId)) {
				throw new IllegalArgumentException("같은 상태로는 전환할 수 없습니다.");
			}

			WorkflowVO transition = new WorkflowVO();
			transition.setFromStatusId(fromStatusId);
			transition.setToStatusId(toStatusId);

			transitionList.add(transition);
		}

		return transitionList;
	}

	// 전환기준 코드 검증
	private void validateApplyTargetCode(String applyTargetCode) {

		validateRequired(applyTargetCode, "전환기준");

		if (!APPLY_TARGET_CODE_LIST.contains(applyTargetCode)) {
			throw new IllegalArgumentException("전환기준 코드가 올바르지 않습니다.");
		}
	}

	// 로그인 사용자 검증
	private void validateUserId(String userId) {

		if (isBlank(userId)) {
			throw new IllegalArgumentException("로그인 정보가 없습니다.");
		}
	}

	// 필수값 검증
	private void validateRequired(String value, String fieldName) {

		if (isBlank(value)) {
			throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	// ==============================
	// 업무흐름 전체조회 추가
	// 역할 x 일감유형별 상태전환 설정 개수 조회
	// ==============================
	@Override
	public List<WorkflowVO> getWorkflowCountList(String userId) {
	    validateUserId(userId);
	    return workflowMapper.selectWorkflowCountList(userId);
	}
	
	// ==============================
	// 업무흐름 복사 추가/수정
	// 선택한 전환 기준만 원본에서 대상으로 복사한다.
	// 대상도 선택한 전환 기준만 삭제 후 복사한다.
	// ==============================
	@Transactional
	@Override
	public void copyWorkflowTransition(WorkflowVO workflowVO, String userId) {

	    validateUserId(userId);

	    workflowVO.setUserId(userId);
	    workflowVO.setCreatedBy(userId);

	    validateRequired(workflowVO.getSourceIssueTypeId(), "원본 일감유형");
	    validateRequired(workflowVO.getSourceRoleId(), "원본 역할");
	    validateRequired(workflowVO.getTargetIssueTypeId(), "대상 일감유형");
	    validateRequired(workflowVO.getTargetRoleId(), "대상 역할");

	    validateCopyApplyTargetCodeList(workflowVO.getCopyApplyTargetCodeList());

	    validateIssueTypeOwner(workflowVO.getSourceIssueTypeId(), userId);
	    validateIssueTypeOwner(workflowVO.getTargetIssueTypeId(), userId);
	    validateRoleOwner(workflowVO.getSourceRoleId(), userId);
	    validateRoleOwner(workflowVO.getTargetRoleId(), userId);

	    if (workflowVO.getSourceIssueTypeId().equals(workflowVO.getTargetIssueTypeId())
	            && workflowVO.getSourceRoleId().equals(workflowVO.getTargetRoleId())) {
	        throw new IllegalArgumentException("원본과 대상이 같습니다.");
	    }

	    int sourceCount = workflowMapper.countSourceWorkflowTransition(workflowVO);

	    if (sourceCount == 0) {
	        throw new IllegalArgumentException("선택한 전환 기준에 복사할 업무흐름 설정이 없습니다.");
	    }

	    workflowMapper.deleteTargetWorkflowTransitionForCopy(workflowVO);
	    workflowMapper.copyWorkflowTransition(workflowVO);
	}
	
	// ==============================
	// 업무흐름 복사 추가
	// 복사할 전환 기준 목록 검증
	// ==============================
	private void validateCopyApplyTargetCodeList(List<String> copyApplyTargetCodeList) {

	    if (copyApplyTargetCodeList == null || copyApplyTargetCodeList.isEmpty()) {
	        throw new IllegalArgumentException("복사할 전환 기준을 선택해주세요.");
	    }

	    for (String applyTargetCode : copyApplyTargetCodeList) {
	        validateApplyTargetCode(applyTargetCode);
	    }
	}
}