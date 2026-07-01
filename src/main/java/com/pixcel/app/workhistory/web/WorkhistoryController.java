package com.pixcel.app.workhistory.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pixcel.app.web.LoginRequiredException;
import com.pixcel.app.workhistory.service.WorkhistoryService;
import com.pixcel.app.workhistory.service.WorkhistoryVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkhistoryController {

	private final WorkhistoryService workhistoryService;

	/*
	 * 작업내역 목록 화면.
	 *
	 * 해야 할 일:
	 * 1. 로그인 사용자 확인
	 * 2. 프로젝트 접근 가능 여부 확인
	 * 3. 검색 조건 정리
	 * 4. 작업내역 page id 선조회
	 * 5. 현재 페이지 작업내역 상세 row 조회
	 * 6. 선택된 필터 표시명 조회
	 * 7. workhistory/workhistory.html에 필요한 model 구성
	 */
	@GetMapping("/project/{projectId}/workhistory")
	public String workhistoryList(
			@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			WorkhistoryVO searchVO,
			Model model) {

		String loginUserId = getLoginUserId(userId);

		try {
			Map<String, Object> pageData = workhistoryService.getWorkhistoryPageData(projectId, searchVO, loginUserId);
			addWorkhistoryModel(model, projectId, searchVO, pageData);
		} catch (IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			addWorkhistoryModel(model, projectId, searchVO, Collections.emptyMap());
		}

		return "workhistory/workhistory";
	}

	/*
	 * 작업내역 필터 옵션 Ajax.
	 *
	 * 해야 할 일:
	 * 1. 로그인 사용자 확인
	 * 2. 프로젝트 접근 가능 여부 확인
	 * 3. 변경유형/변경항목/작업자/유형/상태/우선순위 옵션 조회
	 * 4. 최초 HTML이 아니라 드롭다운을 열 때만 호출되게 유지
	 */
	@GetMapping("/project/{projectId}/workhistory/filter-options")
	@ResponseBody
	public Map<String, Object> workhistoryFilterOptions(
			@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId) {

		String loginUserId = getLoginUserId(userId);

		try {
			return workhistoryService.getWorkhistoryFilterOptions(projectId, loginUserId);
		} catch (IllegalArgumentException e) {
			Map<String, Object> response = new HashMap<>();
			response.put("errorMessage", e.getMessage());
			response.put("changeTypeList", Collections.emptyList());
			response.put("fieldList", Collections.emptyList());
			response.put("changedByList", Collections.emptyList());
			response.put("issueTypeList", Collections.emptyList());
			response.put("issueStatusList", Collections.emptyList());
			response.put("priorityList", Collections.emptyList());
			return response;
		}
	}

	private void addWorkhistoryModel(Model model, String projectId, WorkhistoryVO searchVO,
			Map<String, Object> pageData) {
		if (searchVO == null) {
			searchVO = new WorkhistoryVO();
		}

		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);
		model.addAttribute("searchVO", searchVO);
		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("workHistoryList",
				pageData.getOrDefault("workHistoryList", Collections.emptyList()));

		model.addAttribute("selectedChangeTypeText",
				pageData.getOrDefault("selectedChangeTypeText", "전체"));
		model.addAttribute("selectedFieldText",
				pageData.getOrDefault("selectedFieldText", "전체"));
		model.addAttribute("selectedChangedByText",
				pageData.getOrDefault("selectedChangedByText", "전체"));
		model.addAttribute("selectedIssueTypeText",
				pageData.getOrDefault("selectedIssueTypeText", "전체"));
		model.addAttribute("selectedIssueStatusText",
				pageData.getOrDefault("selectedIssueStatusText", "전체"));
		model.addAttribute("selectedPriorityText",
				pageData.getOrDefault("selectedPriorityText", "전체"));

		/*
		 * 최초 HTML에서는 옵션 목록을 비워둔다.
		 * 옵션은 /workhistory/filter-options Ajax에서 lazy loading한다.
		 */
		model.addAttribute("changeTypeList",
				pageData.getOrDefault("changeTypeList", Collections.emptyList()));
		model.addAttribute("fieldList",
				pageData.getOrDefault("fieldList", Collections.emptyList()));
		model.addAttribute("changedByList",
				pageData.getOrDefault("changedByList", Collections.emptyList()));
		model.addAttribute("issueTypeList",
				pageData.getOrDefault("issueTypeList", Collections.emptyList()));
		model.addAttribute("issueStatusList",
				pageData.getOrDefault("issueStatusList", Collections.emptyList()));
		model.addAttribute("priorityList",
				pageData.getOrDefault("priorityList", Collections.emptyList()));
	}

	private String getLoginUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}

		return userId;
	}
}
