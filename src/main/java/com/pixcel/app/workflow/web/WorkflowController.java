package com.pixcel.app.workflow.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.web.LoginRequiredException;
import com.pixcel.app.workflow.service.WorkflowService;
import com.pixcel.app.workflow.service.WorkflowVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkflowController {

	private final WorkflowService workflowService;

	// ==============================
	// 업무흐름 전체조회 추가/수정
	// 세로: 일감유형
	// 가로: 역할
	// 셀 값: 해당 일감유형 + 역할에 설정된 상태전환 개수
	// ==============================
	@GetMapping("/workflow/list")
	public String workflowList(@CookieValue(value = "userId", required = false) String userId, Model model) {

		String loginUserId = getLoginUserId(userId);

		Map<String, Object> pageData = workflowService.getWorkflowListPageData(loginUserId);
		List<WorkflowVO> issueTypeList = (List<WorkflowVO>) pageData.get("issueTypeList");
		List<WorkflowVO> roleList = (List<WorkflowVO>) pageData.get("roleList");
		List<WorkflowVO> workflowCountList = (List<WorkflowVO>) pageData.get("workflowCountList");

		Map<String, WorkflowVO> workflowCountMap = new LinkedHashMap<>();

		for (WorkflowVO workflowCount : workflowCountList) {
			String countKey = workflowCount.getIssueTypeId() + "|" + workflowCount.getRoleId();
			workflowCountMap.put(countKey, workflowCount);
		}
		


		model.addAttribute("issueTypeList", issueTypeList);
		model.addAttribute("roleList", roleList);
		model.addAttribute("workflowCountMap", workflowCountMap);

		return "workflow/list";
	}

	@GetMapping("/workflow/transition")
	public String workflowTransition(@CookieValue(value = "userId", required = false) String userId,
			WorkflowVO searchVO, Model model) {

		String loginUserId = getLoginUserId(userId);

		Map<String, Object> pageData = workflowService.getWorkflowOptionPageData(loginUserId);
		List<WorkflowVO> issueTypeList = (List<WorkflowVO>) pageData.get("issueTypeList");
		List<WorkflowVO> roleList = (List<WorkflowVO>) pageData.get("roleList");

		setDefaultSearchCondition(searchVO, issueTypeList, roleList);

		searchVO.setUserId(loginUserId);

		List<WorkflowVO> fromStatusList = (List<WorkflowVO>) pageData.get("issueStatusList");
		List<WorkflowVO> toStatusList = filterIssueStatusByClosedYn(fromStatusList, searchVO.getClosedYn());

		List<String> savedTransitionKeyList = new ArrayList<>();

		if (hasSearchCondition(searchVO)) {
			savedTransitionKeyList = workflowService.getSavedTransitionKeyList(searchVO);
		}

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("issueTypeList", issueTypeList);
		model.addAttribute("roleList", roleList);
		model.addAttribute("fromStatusList", fromStatusList);
		model.addAttribute("toStatusList", toStatusList);
		model.addAttribute("savedTransitionKeyList", savedTransitionKeyList);
		model.addAttribute("selectedRoleText",
				getSelectedWorkflowText(searchVO.getRoleId(), roleList, "role", "선택"));
		model.addAttribute("selectedIssueTypeText",
				getSelectedWorkflowText(searchVO.getIssueTypeId(), issueTypeList, "issueType", "선택"));
		model.addAttribute("selectedClosedYnText", getClosedYnText(searchVO.getClosedYn()));

		return "workflow/transition";
	}

	@PostMapping("/workflow/transition/save")
	public String saveWorkflowTransition(@CookieValue(value = "userId", required = false) String userId,
			WorkflowVO workflowVO, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			workflowService.saveWorkflowTransition(workflowVO, loginUserId);
			redirectAttributes.addFlashAttribute("successMessage", "상태전환 설정이 저장되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		redirectAttributes.addAttribute("roleId", workflowVO.getRoleId());
		redirectAttributes.addAttribute("issueTypeId", workflowVO.getIssueTypeId());
		redirectAttributes.addAttribute("closedYn", workflowVO.getClosedYn());
		redirectAttributes.addAttribute("applyTargetCode", workflowVO.getApplyTargetCode());

		return "redirect:/workflow/transition";
	}
	
	// ==============================
	// 업무흐름 복사 추가
	// 복사 화면
	// ==============================
	@GetMapping("/workflow/copy")
	public String workflowCopyForm(@CookieValue(value = "userId", required = false) String userId,
	                               Model model) {

	    String loginUserId = getLoginUserId(userId);

	    if (!model.containsAttribute("workflowVO")) {
	        model.addAttribute("workflowVO", new WorkflowVO());
	    }

	    Map<String, Object> pageData = workflowService.getWorkflowOptionPageData(loginUserId);

	    model.addAttribute("issueTypeList", pageData.get("issueTypeList"));
	    model.addAttribute("roleList", pageData.get("roleList"));

	    return "workflow/copy";
	}

	// ==============================
	// 업무흐름 복사 추가
	// 원본 업무흐름을 대상 조건으로 복사
	// ==============================
	@PostMapping("/workflow/copy")
	public String workflowCopy(@CookieValue(value = "userId", required = false) String userId,
	                           WorkflowVO workflowVO,
	                           RedirectAttributes redirectAttributes) {

	    String loginUserId = getLoginUserId(userId);

	    try {
	        workflowService.copyWorkflowTransition(workflowVO, loginUserId);
	        redirectAttributes.addFlashAttribute("successMessage", "업무흐름이 복사되었습니다.");
	        return "redirect:/workflow/list";
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
	        redirectAttributes.addFlashAttribute("workflowVO", workflowVO);
	        return "redirect:/workflow/copy";
	    }
	}

	private void setDefaultSearchCondition(WorkflowVO searchVO, List<WorkflowVO> issueTypeList,
			List<WorkflowVO> roleList) {

		if (isBlank(searchVO.getRoleId()) && roleList != null && !roleList.isEmpty()) {
			searchVO.setRoleId(roleList.get(0).getRoleId());
		}

		if (isBlank(searchVO.getIssueTypeId()) && issueTypeList != null && !issueTypeList.isEmpty()) {
			searchVO.setIssueTypeId(issueTypeList.get(0).getIssueTypeId());
		}

		if (isBlank(searchVO.getApplyTargetCode())) {
			searchVO.setApplyTargetCode("j001");
		}
	}

	private boolean hasSearchCondition(WorkflowVO searchVO) {
		return !isBlank(searchVO.getIssueTypeId()) && !isBlank(searchVO.getRoleId())
				&& !isBlank(searchVO.getApplyTargetCode());
	}

	private List<WorkflowVO> filterIssueStatusByClosedYn(List<WorkflowVO> issueStatusList, String closedYn) {
		if (isBlank(closedYn)) {
			return issueStatusList;
		}

		List<WorkflowVO> filteredStatusList = new ArrayList<>();

		for (WorkflowVO issueStatus : issueStatusList) {
			if (closedYn.equals(issueStatus.getClosedYn())) {
				filteredStatusList.add(issueStatus);
			}
		}

		return filteredStatusList;
	}

	private String getLoginUserId(String userId) {
		if (isBlank(userId)) {
			throw new LoginRequiredException();
		}

		return userId;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String getSelectedWorkflowText(String selectedValue, List<WorkflowVO> optionList, String optionType,
			String defaultText) {
		if (isBlank(selectedValue) || optionList == null || optionList.isEmpty()) {
			return defaultText;
		}

		for (WorkflowVO option : optionList) {
			String value = "role".equals(optionType) ? option.getRoleId() : option.getIssueTypeId();

			if (selectedValue.equals(value)) {
				String label = "role".equals(optionType) ? option.getRoleName() : option.getIssueTypeName();
				return isBlank(label) ? defaultText : label;
			}
		}

		return defaultText;
	}

	private String getClosedYnText(String closedYn) {
		if ("N".equals(closedYn)) {
			return "미완료";
		}

		if ("Y".equals(closedYn)) {
			return "완료";
		}

		return "전체";
	}
}
