package com.pixcel.app.workhistory.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pixcel.app.workhistory.mapper.WorkhistoryMapper;
import com.pixcel.app.workhistory.service.WorkhistoryService;
import com.pixcel.app.workhistory.service.WorkhistoryVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkhistoryServiceImpl implements WorkhistoryService {

	private static final int DEFAULT_PAGE = 1;
	private static final int PAGE_SIZE = 10;
	private static final int PAGE_BLOCK_SIZE = 5;

	private static final String OPTION_CHANGE_TYPE = "CHANGE_TYPE";
	private static final String OPTION_FIELD = "FIELD";
	private static final String OPTION_CHANGED_BY = "CHANGED_BY";
	private static final String OPTION_ISSUE_TYPE = "ISSUE_TYPE";
	private static final String OPTION_STATUS = "STATUS";
	private static final String OPTION_PRIORITY = "PRIORITY";

	private final WorkhistoryMapper workhistoryMapper;

	@Override
	public Map<String, Object> getWorkhistoryPageData(String projectId, WorkhistoryVO searchVO, String userId) {
		validateUserId(userId);

		if (searchVO == null) {
			searchVO = new WorkhistoryVO();
		}

		normalizeSearchVO(projectId, userId, searchVO);
		validateDateRange(searchVO);
		preparePageProbeRange(searchVO);

		/*
		 * TODO 1. 프로젝트 접근 확인 + projectInfo 조회
		 *
		 * XML 작성 후 연결할 코드:
		 * WorkhistoryVO projectInfo = workhistoryMapper.selectProjectAccess(projectId, userId);
		 * if (projectInfo == null) {
		 *     throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		 * }
		 */
		WorkhistoryVO projectInfo = buildTemporaryProjectInfo(projectId);

		/*
		 * TODO 2. 작업내역 ID 선조회
		 *
		 * XML 작성 후 연결할 코드:
		 * List<WorkhistoryVO> pageIdList = safeList(workhistoryMapper.selectWorkhistoryPageIds(searchVO));
		 *
		 * 조회 기준:
		 * - history_id, row_no만 조회
		 * - 전체 COUNT(*) 금지
		 * - startRow ~ endRow까지만 조회
		 * - order by changed_at desc, history_id desc
		 */
		List<WorkhistoryVO> pageIdList = Collections.emptyList();

		applyPagination(searchVO, pageIdList);
		List<String> currentPageHistoryIdList = getCurrentPageHistoryIdList(searchVO, pageIdList);

		/*
		 * TODO 3. 현재 페이지 상세 row 조회
		 *
		 * XML 작성 후 연결할 코드:
		 * List<WorkhistoryVO> workHistoryList = currentPageHistoryIdList.isEmpty()
		 *     ? Collections.emptyList()
		 *     : safeList(workhistoryMapper.selectWorkhistoryRowsByIds(projectId, currentPageHistoryIdList));
		 *
		 * 조회 기준:
		 * - currentPageHistoryIdList에 있는 history_id만 상세 조회
		 * - issue, users, issue_type, issue_status, setting_code는 필요한 컬럼만 join
		 */
		List<WorkhistoryVO> workHistoryList = Collections.emptyList();

		/*
		 * TODO 4. 선택된 필터 표시명 조회
		 *
		 * XML 작성 후 연결할 코드:
		 * List<WorkhistoryVO> selectedFilterRows = hasSelectedFilter(searchVO)
		 *     ? safeList(workhistoryMapper.selectSelectedFilterRows(searchVO))
		 *     : Collections.emptyList();
		 *
		 * 주의:
		 * - 선택된 필터가 없으면 실행하지 않는다.
		 * - 필터 전체 옵션 조회와 분리한다.
		 */
		List<WorkhistoryVO> selectedFilterRows = Collections.emptyList();
		Map<String, String> selectedTextMap = toSelectedTextMap(selectedFilterRows);

		Map<String, Object> pageData = new HashMap<>();
		pageData.put("projectInfo", projectInfo);
		pageData.put("workHistoryList", workHistoryList);
		pageData.put("selectedChangeTypeText", selectedTextMap.getOrDefault(OPTION_CHANGE_TYPE, "전체"));
		pageData.put("selectedFieldText", selectedTextMap.getOrDefault(OPTION_FIELD, "전체"));
		pageData.put("selectedChangedByText", selectedTextMap.getOrDefault(OPTION_CHANGED_BY, "전체"));
		pageData.put("selectedIssueTypeText", selectedTextMap.getOrDefault(OPTION_ISSUE_TYPE, "전체"));
		pageData.put("selectedIssueStatusText", selectedTextMap.getOrDefault(OPTION_STATUS, "전체"));
		pageData.put("selectedPriorityText", selectedTextMap.getOrDefault(OPTION_PRIORITY, "전체"));

		/*
		 * 최초 HTML에서는 필터 옵션 전체를 싣지 않는다.
		 * 화면에서 드롭다운을 열 때 /workhistory/filter-options Ajax로 조회한다.
		 */
		pageData.put("changeTypeList", Collections.emptyList());
		pageData.put("fieldList", Collections.emptyList());
		pageData.put("changedByList", Collections.emptyList());
		pageData.put("issueTypeList", Collections.emptyList());
		pageData.put("issueStatusList", Collections.emptyList());
		pageData.put("priorityList", Collections.emptyList());

		return pageData;
	}

	@Override
	public Map<String, Object> getWorkhistoryFilterOptions(String projectId, String userId) {
		validateUserId(userId);
		validateProjectId(projectId);

		/*
		 * TODO 1. 프로젝트 접근 확인
		 *
		 * XML 작성 후 연결할 코드:
		 * WorkhistoryVO projectInfo = workhistoryMapper.selectProjectAccess(projectId, userId);
		 * if (projectInfo == null) {
		 *     throw new IllegalArgumentException("접근 권한이 없는 프로젝트입니다.");
		 * }
		 */

		/*
		 * TODO 2. 필터 옵션 Ajax 조회
		 *
		 * XML 작성 후 연결할 코드:
		 * List<WorkhistoryVO> optionRows = safeList(workhistoryMapper.selectFilterOptionRows(projectId, userId));
		 *
		 * 조회 기준:
		 * - optionGroup, optionValue, optionLabel 형태로 통일
		 * - 최초 화면 진입이 아니라 Ajax에서만 실행
		 * - 느리면 프로젝트 단위 캐싱 후보
		 */
		List<WorkhistoryVO> optionRows = Collections.emptyList();

		Map<String, List<WorkhistoryVO>> optionMap = groupOptionRows(optionRows);

		Map<String, Object> response = new HashMap<>();
		response.put("changeTypeList", optionMap.getOrDefault(OPTION_CHANGE_TYPE, Collections.emptyList()));
		response.put("fieldList", optionMap.getOrDefault(OPTION_FIELD, Collections.emptyList()));
		response.put("changedByList", optionMap.getOrDefault(OPTION_CHANGED_BY, Collections.emptyList()));
		response.put("issueTypeList", optionMap.getOrDefault(OPTION_ISSUE_TYPE, Collections.emptyList()));
		response.put("issueStatusList", optionMap.getOrDefault(OPTION_STATUS, Collections.emptyList()));
		response.put("priorityList", optionMap.getOrDefault(OPTION_PRIORITY, Collections.emptyList()));

		return response;
	}

	private void normalizeSearchVO(String projectId, String userId, WorkhistoryVO searchVO) {
		validateProjectId(projectId);

		searchVO.setProjectId(projectId);
		searchVO.setUserId(userId);
		searchVO.setKeyword(trimToNull(searchVO.getKeyword()));

		if (searchVO.getPage() == null || searchVO.getPage() < 1) {
			searchVO.setPage(DEFAULT_PAGE);
		}

		searchVO.setPageSize(PAGE_SIZE);
	}

	private void validateDateRange(WorkhistoryVO searchVO) {
		if (searchVO.getStartDate() == null || searchVO.getEndDate() == null) {
			return;
		}

		if (searchVO.getEndDate().isBefore(searchVO.getStartDate())) {
			throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
		}
	}

	private void preparePageProbeRange(WorkhistoryVO searchVO) {
		int page = searchVO.getPage();
		int blockStartPage = ((page - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
		int blockStartRow = (blockStartPage - 1) * PAGE_SIZE + 1;
		int blockEndRow = blockStartRow + (PAGE_BLOCK_SIZE * PAGE_SIZE);

		searchVO.setStartRow(blockStartRow);
		searchVO.setEndRow(blockEndRow);
	}

	private void applyPagination(WorkhistoryVO searchVO, List<WorkhistoryVO> pageIdList) {
		int page = searchVO.getPage();
		int blockStartPage = ((page - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
		int fetchedCount = pageIdList == null ? 0 : pageIdList.size();
		boolean hasNextBlock = fetchedCount > PAGE_BLOCK_SIZE * PAGE_SIZE;
		int visibleCount = hasNextBlock ? PAGE_BLOCK_SIZE * PAGE_SIZE : fetchedCount;
		int pageCountInBlock = Math.max(1, (int) Math.ceil(visibleCount / (double) PAGE_SIZE));
		int lastPageInBlock = blockStartPage + pageCountInBlock - 1;

		List<Integer> pageList = new ArrayList<>();
		for (int pageNo = blockStartPage; pageNo <= lastPageInBlock; pageNo++) {
			pageList.add(pageNo);
		}

		searchVO.setPageList(pageList);
		searchVO.setHasPreviousPage(page > 1);
		searchVO.setHasNextPage(hasNextBlock || page < lastPageInBlock);
	}

	private List<String> getCurrentPageHistoryIdList(WorkhistoryVO searchVO, List<WorkhistoryVO> pageIdList) {
		if (pageIdList == null || pageIdList.isEmpty()) {
			return Collections.emptyList();
		}

		int currentStartRow = (searchVO.getPage() - 1) * PAGE_SIZE + 1;
		int currentEndRow = currentStartRow + PAGE_SIZE - 1;
		List<String> historyIdList = new ArrayList<>();

		for (WorkhistoryVO row : pageIdList) {
			if (row == null || row.getRowNo() == null || isBlank(row.getHistoryId())) {
				continue;
			}

			if (row.getRowNo() >= currentStartRow && row.getRowNo() <= currentEndRow) {
				historyIdList.add(row.getHistoryId());
			}
		}

		return historyIdList;
	}

	private Map<String, String> toSelectedTextMap(List<WorkhistoryVO> selectedFilterRows) {
		if (selectedFilterRows == null || selectedFilterRows.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, List<String>> labelMap = new LinkedHashMap<>();

		for (WorkhistoryVO row : selectedFilterRows) {
			if (row == null || isBlank(row.getOptionGroup()) || isBlank(row.getOptionLabel())) {
				continue;
			}

			labelMap.computeIfAbsent(row.getOptionGroup(), key -> new ArrayList<>()).add(row.getOptionLabel());
		}

		Map<String, String> selectedTextMap = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : labelMap.entrySet()) {
			selectedTextMap.put(entry.getKey(), String.join(", ", entry.getValue()));
		}

		return selectedTextMap;
	}

	private Map<String, List<WorkhistoryVO>> groupOptionRows(List<WorkhistoryVO> optionRows) {
		if (optionRows == null || optionRows.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, List<WorkhistoryVO>> optionMap = new LinkedHashMap<>();

		for (WorkhistoryVO row : optionRows) {
			if (row == null || isBlank(row.getOptionGroup())) {
				continue;
			}

			optionMap.computeIfAbsent(row.getOptionGroup(), key -> new ArrayList<>()).add(row);
		}

		return optionMap;
	}

	private boolean hasSelectedFilter(WorkhistoryVO searchVO) {
		if (searchVO == null) {
			return false;
		}

		return hasValue(searchVO.getChangeTypeCodeList())
				|| hasValue(searchVO.getFieldCodeList())
				|| hasValue(searchVO.getChangedByList())
				|| hasValue(searchVO.getIssueTypeIdList())
				|| hasValue(searchVO.getIssueStatusIdList())
				|| hasValue(searchVO.getSettingCodeIdList());
	}

	private boolean hasValue(List<String> valueList) {
		return valueList != null && !valueList.isEmpty();
	}

	private List<WorkhistoryVO> safeList(List<WorkhistoryVO> rows) {
		return rows == null ? Collections.emptyList() : rows;
	}

	private WorkhistoryVO buildTemporaryProjectInfo(String projectId) {
		WorkhistoryVO projectInfo = new WorkhistoryVO();
		projectInfo.setProjectId(projectId);
		projectInfo.setProjectName("프로젝트");
		return projectInfo;
	}

	private void validateUserId(String userId) {
		if (isBlank(userId)) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
	}

	private void validateProjectId(String projectId) {
		if (isBlank(projectId)) {
			throw new IllegalArgumentException("프로젝트를 선택해주세요.");
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
