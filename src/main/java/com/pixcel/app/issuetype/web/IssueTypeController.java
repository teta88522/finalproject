package com.pixcel.app.issuetype.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.issuetype.service.IssueTypeService;
import com.pixcel.app.issuetype.service.IssueTypeVO;
import com.pixcel.app.web.LoginRequiredException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IssueTypeController {

	private final IssueTypeService issueTypeService;

	// 일감유형 목록 화면으로 이동한다.
	@GetMapping("/issuetype/list")
	public String issueTypeList(@CookieValue(value = "userId", required = false) String userId, IssueTypeVO searchVO,
			Model model) {

		String loginUserId = getLoginUserId(userId);

		searchVO.setUserId(loginUserId);
		Map<String, Object> pageData = issueTypeService.getIssueTypeListPageData(searchVO);
		List<IssueTypeVO> issueStatusList = (List<IssueTypeVO>) pageData.get("issueStatusList");

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("issueTypeList", pageData.get("issueTypeList"));
		model.addAttribute("issueStatusList", issueStatusList);
		model.addAttribute("selectedInitialStatusText",
				getSelectedStatusText(searchVO.getInitialStatusIdList(), issueStatusList, "전체"));

		return "issuetype/list";
	}

	// 일감유형 요약 화면으로 이동한다.
	@GetMapping("/issuetype/summary")
	public String issueTypeSummary(@CookieValue(value = "userId", required = false) String userId, IssueTypeVO searchVO,
			Model model) {

		String loginUserId = getLoginUserId(userId);

		searchVO.setUserId(loginUserId);

		IssueTypeVO selectSearchVO = new IssueTypeVO();
		selectSearchVO.setUserId(loginUserId);

		int selectedCount = 0;

		if (searchVO.getIssueTypeIdList() != null) {
			selectedCount = searchVO.getIssueTypeIdList().size();
		}

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("issueTypeSelectList", issueTypeService.getIssueTypeList(selectSearchVO));
		model.addAttribute("issueTypeSummaryList", issueTypeService.getIssueTypeSummaryList(searchVO));
		model.addAttribute("selectedCount", selectedCount);
		model.addAttribute("maxSelectCount", 5);

		return "issuetype/summary";
	}

	// 일감유형 생성 화면으로 이동한다.
	@GetMapping("/issuetype/create")
	public String issueTypeCreateForm(@CookieValue(value = "userId", required = false) String userId, Model model) {

		String loginUserId = getLoginUserId(userId);

		if (!model.containsAttribute("issueType")) {
			model.addAttribute("issueType", new IssueTypeVO());
		}

		addFormModel(model, loginUserId);

		return "issuetype/create";
	}

	// 신규 일감유형을 등록한다.
	@PostMapping("/issuetype/create")
	public String issueTypeCreate(@CookieValue(value = "userId", required = false) String userId, IssueTypeVO issueType,
			RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueTypeService.createIssueType(issueType, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감유형이 등록되었습니다.");

			return "redirect:/issuetype/list";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issueType", issueType);

			return "redirect:/issuetype/create";
		}
	}

	// 일감유형 복사 화면으로 이동한다.
	@GetMapping("/issuetype/copy")
	public String issueTypeCopyForm(@CookieValue(value = "userId", required = false) String userId,
			@RequestParam("issueTypeId") String issueTypeId, Model model, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			IssueTypeVO issueType = issueTypeService.getIssueTypeDetail(issueTypeId, loginUserId);

			issueType.setIssueTypeId(null);
			issueType.setIssueTypeName(issueType.getIssueTypeName() + " - 복사");

			model.addAttribute("issueType", issueType);
			addFormModel(model, loginUserId);

			return "issuetype/copy";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/issuetype/list";
		}
	}

	// 기존 일감유형을 복사하여 신규 일감유형으로 등록한다.
	@PostMapping("/issuetype/copy")
	public String issueTypeCopy(@CookieValue(value = "userId", required = false) String userId, IssueTypeVO issueType,
			RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueTypeService.copyIssueType(issueType, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감유형이 복사되었습니다.");

			return "redirect:/issuetype/list";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issueType", issueType);

			return "redirect:/issuetype/create";
		}
	}

	// 사용 중이지 않은 일감유형을 삭제한다.
	@PostMapping("/issuetype/delete")
	public String issueTypeDelete(@CookieValue(value = "userId", required = false) String userId,
			@RequestParam("issueTypeId") String issueTypeId, RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issueTypeService.removeIssueType(issueTypeId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감유형이 삭제되었습니다.");

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/issuetype/list";
	}

	// 일감유형 생성/복사 화면에 필요한 공통 데이터를 담는다.
	private void addFormModel(Model model, String userId) {

		Map<String, Object> pageData = issueTypeService.getIssueTypeFormPageData(userId);

		model.addAttribute("issueStatusList", pageData.get("issueStatusList"));
		model.addAttribute("projectList", pageData.get("projectList"));
	}

	// 현재 로그인 사용자 ID를 반환한다.
	private String getLoginUserId(String userId) {

		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}

		return userId;
	}

	private String getSelectedStatusText(List<String> selectedStatusIdList, List<IssueTypeVO> issueStatusList,
			String defaultText) {
		if (selectedStatusIdList == null || selectedStatusIdList.isEmpty() || issueStatusList == null
				|| issueStatusList.isEmpty()) {
			return defaultText;
		}

		List<String> selectedLabelList = new ArrayList<>();

		for (IssueTypeVO status : issueStatusList) {
			if (selectedStatusIdList.contains(status.getIssueStatusId())) {
				String label = status.getStatusName();

				if (label != null) {
					selectedLabelList.add(label);
				}
			}
		}

		return selectedLabelList.isEmpty() ? defaultText : String.join(", ", selectedLabelList);
	}
}
