package com.pixcel.app.timelog.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.timelog.mapper.TimelogMapper;
import com.pixcel.app.timelog.service.TimelogService;
import com.pixcel.app.timelog.service.TimelogVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimelogServiceImpl implements TimelogService {

	private final TimelogMapper timelogMapper;

	private static final String PERMISSION_ISSUE_UPDATE_CODE = "p009";
	private static final String PERMISSION_ISSUE_UPDATE_OWN_CODE = "p011";
	private static final String OPTION_PROJECT = "PROJECT";
	private static final String OPTION_WORK_TYPE = "WORK_TYPE";
	private static final String OPTION_ISSUE = "ISSUE";

	@Override
	public Map<String, Object> getCreatePageData(String projectId, String userId, String issueSearchKeyword,
			String selectedIssueId) {
		validateUserId(userId);
		String checkedKeyword = trimToNull(issueSearchKeyword);
		String checkedIssueId = trimToNull(selectedIssueId);

		List<TimelogVO> rows = timelogMapper.selectTimelogFormRows(projectId, userId, PERMISSION_ISSUE_UPDATE_CODE,
				PERMISSION_ISSUE_UPDATE_OWN_CODE, checkedKeyword, checkedIssueId);
		TimelogVO projectInfo = firstOption(rows, OPTION_PROJECT);

		if (projectInfo == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", projectInfo);
		pageData.put("workTypeList", filterOptionRows(rows, OPTION_WORK_TYPE));
		pageData.put("issueList", filterOptionRows(rows, OPTION_ISSUE));

		return pageData;
	}

	@Override
	@Transactional
	public void createTimelog(TimelogVO timelog, String userId) {
		validateUserId(userId);
		validateBasicTimelog(timelog);
		normalizeTimelog(timelog);

		TimelogVO validation = timelogMapper.selectTimelogSaveValidation(timelog.getProjectId(), timelog.getIssueId(),
				timelog.getSettingCodeId(), userId, PERMISSION_ISSUE_UPDATE_CODE, PERMISSION_ISSUE_UPDATE_OWN_CODE);
		validateSaveCondition(validation);

		timelog.setTimeLogId("TIME_LOG_" + UUID.randomUUID());
		timelog.setUserId(userId);

		timelogMapper.insertTimelog(timelog);
	}

	@Override
	public Map<String, Object> getDetailPageData(String projectId, String issueId, String timeLogId, String userId) {
		TimelogVO timelog = selectTimelogDetailOrThrow(projectId, issueId, timeLogId, userId);
		boolean canUpdateTimelog = "Y".equals(timelog.getUpdatePermissionYn());

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", timelog);
		pageData.put("timelog", timelog);
		pageData.put("canUpdateTimelog", canUpdateTimelog);

		if (canUpdateTimelog) {
			List<TimelogVO> rows = timelogMapper.selectTimelogFormRows(projectId, userId, PERMISSION_ISSUE_UPDATE_CODE,
					PERMISSION_ISSUE_UPDATE_OWN_CODE, null, timelog.getIssueId());

			pageData.put("workTypeList", filterOptionRows(rows, OPTION_WORK_TYPE));
			pageData.put("issueList", filterOptionRows(rows, OPTION_ISSUE));
		} else {
			pageData.put("workTypeList", Collections.emptyList());
			pageData.put("issueList", Collections.emptyList());
		}

		return pageData;
	}

	@Override
	public Map<String, Object> getUpdatePageData(String projectId, String issueId, String timeLogId, String userId,
			String issueSearchKeyword, String selectedIssueId) {
		TimelogVO timelog = selectTimelogDetailOrThrow(projectId, issueId, timeLogId, userId);

		if (!"Y".equals(timelog.getUpdatePermissionYn())) {
			throw new IllegalArgumentException("소요시간을 수정할 권한이 없습니다.");
		}

		String checkedKeyword = trimToNull(issueSearchKeyword);
		String checkedSelectedIssueId = trimToNull(selectedIssueId);
		if (checkedSelectedIssueId == null) {
			checkedSelectedIssueId = timelog.getIssueId();
		}
		List<TimelogVO> rows = timelogMapper.selectTimelogFormRows(projectId, userId, PERMISSION_ISSUE_UPDATE_CODE,
				PERMISSION_ISSUE_UPDATE_OWN_CODE, checkedKeyword, checkedSelectedIssueId);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", timelog);
		pageData.put("timelog", timelog);
		pageData.put("workTypeList", filterOptionRows(rows, OPTION_WORK_TYPE));
		pageData.put("issueList", filterOptionRows(rows, OPTION_ISSUE));

		return pageData;
	}

	@Override
	@Transactional
	public void updateTimelog(TimelogVO timelog, String userId) {
		validateUserId(userId);
		validateBasicTimelog(timelog);
		normalizeTimelog(timelog);

		TimelogVO current = selectTimelogDetailOrThrow(timelog.getProjectId(), timelog.getOriginalIssueId(),
				timelog.getTimeLogId(), userId);

		if (!"Y".equals(current.getUpdatePermissionYn())) {
			throw new IllegalArgumentException("소요시간을 수정할 권한이 없습니다.");
		}

		TimelogVO validation = timelogMapper.selectTimelogSaveValidation(timelog.getProjectId(), timelog.getIssueId(),
				timelog.getSettingCodeId(), userId, PERMISSION_ISSUE_UPDATE_CODE, PERMISSION_ISSUE_UPDATE_OWN_CODE);
		validateSaveCondition(validation);

		int updateCount = timelogMapper.updateTimelog(timelog);
		if (updateCount == 0) {
			throw new IllegalArgumentException("수정할 소요시간 정보가 없습니다.");
		}

	}

	@Override
	@Transactional
	public void deleteTimelog(String projectId, String issueId, String timeLogId, String userId) {
		TimelogVO timelog = selectTimelogDetailOrThrow(projectId, issueId, timeLogId, userId);

		if (!"Y".equals(timelog.getUpdatePermissionYn())) {
			throw new IllegalArgumentException("소요시간을 삭제할 권한이 없습니다.");
		}

		int deleteCount = timelogMapper.deleteTimelog(timeLogId);
		if (deleteCount == 0) {
			throw new IllegalArgumentException("삭제할 소요시간 정보가 없습니다.");
		}

	}

	@Override
	public Map<String, Object> getIssueTimelogSummary(String projectId, String issueId) {
		List<TimelogVO> rows = timelogMapper.selectIssueTimelogSummaryRows(projectId, issueId);
		if (rows == null) {
			rows = Collections.emptyList();
		}

		Integer totalHours = rows.isEmpty() || rows.get(0).getTotalHours() == null ? 0 : rows.get(0).getTotalHours();
		Integer totalCount = rows.isEmpty() || rows.get(0).getTotalCount() == null ? 0 : rows.get(0).getTotalCount();

		Map<String, Object> summary = new HashMap<>();
		summary.put("timeLogSummaryList", rows);
		summary.put("timeLogTotalHours", totalHours);
		summary.put("timeLogTotalCount", totalCount);

		return summary;
	}

	private TimelogVO selectTimelogDetailOrThrow(String projectId, String issueId, String timeLogId, String userId) {
		validateUserId(userId);

		if (isBlank(projectId) || isBlank(issueId) || isBlank(timeLogId)) {
			throw new IllegalArgumentException("조회할 소요시간 정보가 없습니다.");
		}

		TimelogVO timelog = timelogMapper.selectTimelogDetail(projectId, issueId, timeLogId, userId,
				PERMISSION_ISSUE_UPDATE_CODE, PERMISSION_ISSUE_UPDATE_OWN_CODE);

		if (timelog == null) {
			throw new IllegalArgumentException("조회할 수 있는 소요시간 정보가 없습니다.");
		}

		return timelog;
	}

	private void validateSaveCondition(TimelogVO validation) {
		if (validation == null) {
			throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		}

		if (validation.getIssueAccessCount() == null || validation.getIssueAccessCount() == 0) {
			throw new IllegalArgumentException("선택할 수 없는 일감입니다.");
		}

		if (validation.getWorkTypeCount() == null || validation.getWorkTypeCount() == 0) {
			throw new IllegalArgumentException("사용할 수 없는 작업분류입니다.");
		}
	}

	private void validateBasicTimelog(TimelogVO timelog) {
		if (timelog == null) {
			throw new IllegalArgumentException("등록할 소요시간 정보가 없습니다.");
		}

		if (isBlank(timelog.getProjectId())) {
			throw new IllegalArgumentException("프로젝트를 선택해주세요.");
		}

		if (isBlank(timelog.getIssueId())) {
			throw new IllegalArgumentException("일감을 선택해주세요.");
		}

		if (isBlank(timelog.getSettingCodeId())) {
			throw new IllegalArgumentException("작업분류를 선택해주세요.");
		}

		if (timelog.getWorkDate() == null) {
			throw new IllegalArgumentException("작업일을 입력해주세요.");
		}

		if (timelog.getHours() == null || timelog.getHours() <= 0 || timelog.getHours() > 99999) {
			throw new IllegalArgumentException("소요시간은 1부터 99999까지 입력할 수 있습니다.");
		}

		String description = trimToNull(timelog.getDescription());
		if (description == null) {
			throw new IllegalArgumentException("작업 내용을 입력해주세요.");
		}

		if (description.length() > 1000) {
			throw new IllegalArgumentException("작업 내용은 1000자 이하로 입력해주세요.");
		}
	}

	private void normalizeTimelog(TimelogVO timelog) {
		timelog.setProjectId(trimToNull(timelog.getProjectId()));
		timelog.setIssueId(trimToNull(timelog.getIssueId()));
		timelog.setSettingCodeId(trimToNull(timelog.getSettingCodeId()));
		timelog.setDescription(trimToNull(timelog.getDescription()));
	}

	private TimelogVO firstOption(List<TimelogVO> rows, String optionGroup) {
		if (rows == null || rows.isEmpty()) {
			return null;
		}

		return rows.stream().filter(row -> optionGroup.equals(row.getOptionGroup())).findFirst().orElse(null);
	}

	private List<TimelogVO> filterOptionRows(List<TimelogVO> rows, String optionGroup) {
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}

		return rows.stream().filter(row -> optionGroup.equals(row.getOptionGroup())).collect(Collectors.toList());
	}

	private void validateUserId(String userId) {
		if (isBlank(userId)) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isEmpty() ? null : trimmedValue;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
