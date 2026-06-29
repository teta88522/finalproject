package com.pixcel.app.issuestatus.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.issuestatus.service.IssueStatusService;
import com.pixcel.app.issuestatus.service.IssueStatusVO;
import com.pixcel.app.web.LoginRequiredException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IssueStatusController {

	private final IssueStatusService issueStatusService;

	// 일감 상태 목록 화면으로 이동한다.
	@GetMapping("/issuestatus/list")
	public String issueStatusList(@CookieValue(value = "userId", required = false) String userId,
			IssueStatusVO searchVO, Model model) {

		String loginUserId = getLoginUserId(userId);

		searchVO.setUserId(loginUserId);

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("selectedClosedYnText",
				getSelectedText(searchVO.getClosedYnList(), getClosedYnNameMap(), "전체"));
		model.addAttribute("issueStatusList", issueStatusService.getIssueStatusSearchList(searchVO));

		return "issuestatus/list";
	}

	// 일감 상태 등록 화면으로 이동한다.
	@GetMapping("/issuestatus/create")
	public String issueStatusCreateForm(@CookieValue(value = "userId", required = false) String userId, Model model) {

		getLoginUserId(userId);

		if (!model.containsAttribute("issueStatus")) {
			model.addAttribute("issueStatus", new IssueStatusVO());
		}

		return "issuestatus/create";
	}

	// 신규 일감 상태를 등록한다.
	@PostMapping("/issuestatus/create")
	public String issueStatusCreate(@CookieValue(value = "userId", required = false) String userId,
			IssueStatusVO issueStatus, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueStatusService.createIssueStatus(issueStatus, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감 상태가 등록되었습니다.");

			return "redirect:/issuestatus/list";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issueStatus", issueStatus);

			return "redirect:/issuestatus/create";
		}
	}

	// 일감 상태 수정 화면으로 이동한다.
	@GetMapping("/issuestatus/update")
	public String issueStatusUpdateForm(@CookieValue(value = "userId", required = false) String userId,
			@RequestParam("issueStatusId") String issueStatusId, Model model, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			IssueStatusVO issueStatus = issueStatusService.getIssueStatusDetail(issueStatusId, loginUserId);
			model.addAttribute("issueStatus", issueStatus);

			return "issuestatus/update";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/issuestatus/list";
		}
	}

	// 일감 상태를 수정한다.
	@PostMapping("/issuestatus/update")
	public String issueStatusUpdate(@CookieValue(value = "userId", required = false) String userId,
			IssueStatusVO issueStatus, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueStatusService.modifyIssueStatus(issueStatus, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감 상태가 수정되었습니다.");

			return "redirect:/issuestatus/list";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issueStatus", issueStatus);

			return "redirect:/issuestatus/update?issueStatusId=" + issueStatus.getIssueStatusId();
		}
	}

	// 사용 중이지 않은 일감 상태를 삭제한다.
	@PostMapping("/issuestatus/delete")
	public String issueStatusDelete(@CookieValue(value = "userId", required = false) String userId,
			@RequestParam("issueStatusId") String issueStatusId, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueStatusService.removeIssueStatus(issueStatusId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감 상태가 삭제되었습니다.");

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/issuestatus/list";
	}

	// 현재 로그인 사용자 ID를 반환한다.
	private String getLoginUserId(String userId) {

		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}

		return userId;
	}

	private Map<String, String> getClosedYnNameMap() {
		Map<String, String> closedYnNameMap = new LinkedHashMap<>();
		closedYnNameMap.put("N", "진행");
		closedYnNameMap.put("Y", "종료");
		return closedYnNameMap;
	}

	private String getSelectedText(List<String> selectedValueList, Map<String, String> labelMap, String defaultText) {
		if (selectedValueList == null || selectedValueList.isEmpty()) {
			return defaultText;
		}

		List<String> selectedLabelList = new ArrayList<>();

		for (String selectedValue : selectedValueList) {
			String label = labelMap.get(selectedValue);

			if (label != null) {
				selectedLabelList.add(label);
			}
		}

		return selectedLabelList.isEmpty() ? defaultText : String.join(", ", selectedLabelList);
	}
}
