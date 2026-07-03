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

	/* 작업내역 목록 화면 구성. */
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

	/* 작업내역 필터 옵션 Ajax 조회. */
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

		/* 최초 HTML용 빈 필터 옵션 목록 설정. */
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
