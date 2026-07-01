package com.pixcel.app.timelog.web;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.timelog.service.TimelogService;
import com.pixcel.app.timelog.service.TimelogVO;
import com.pixcel.app.web.LoginRequiredException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TimelogController {

	private final TimelogService timelogService;

	@GetMapping("/project/{projectId}/issues/timelog/create")
	public String createForm(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@RequestParam(value = "issueId", required = false) String issueId,
			@RequestParam(value = "issueSearchKeyword", required = false) String issueSearchKeyword,
			Model model) {
		String loginUserId = getLoginUserId(userId);

		TimelogVO timelog = getFormTimelog(model, projectId, issueId);
		Map<String, Object> pageData = timelogService.getCreatePageData(projectId, loginUserId, issueSearchKeyword,
				timelog.getIssueId());

		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("selectedProjectId", projectId);
		model.addAttribute("timelog", timelog);
		model.addAttribute("workTypeList", pageData.get("workTypeList"));
		model.addAttribute("issueList", pageData.get("issueList"));
		model.addAttribute("issueSearchKeyword", issueSearchKeyword);

		return "timelog/create";
	}

	@PostMapping("/project/{projectId}/issues/timelog/create")
	public String create(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			TimelogVO timelog,
			RedirectAttributes redirectAttributes) {
		String loginUserId = getLoginUserId(userId);
		timelog.setProjectId(projectId);

		try {
			timelogService.createTimelog(timelog, loginUserId);
			redirectAttributes.addFlashAttribute("message", "소요시간이 등록되었습니다.");
			return "redirect:/project/" + projectId + "/issues/" + timelog.getIssueId() + "#timeLogSection";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("timelog", timelog);
			return "redirect:/project/" + projectId + "/issues/timelog/create" + queryParam("issueId", timelog.getIssueId());
		}
	}

	@GetMapping("/project/{projectId}/issues/{issueId}/timelog/{timeLogId}")
	public String detail(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@PathVariable("timeLogId") String timeLogId,
			Model model) {
		String loginUserId = getLoginUserId(userId);
		Map<String, Object> pageData = timelogService.getDetailPageData(projectId, issueId, timeLogId, loginUserId);

		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("selectedProjectId", projectId);
		model.addAttribute("timelog", pageData.get("timelog"));
		model.addAttribute("canUpdateTimelog", pageData.get("canUpdateTimelog"));
		model.addAttribute("workTypeList", pageData.get("workTypeList"));
		model.addAttribute("issueList", pageData.get("issueList"));

		return "timelog/detail";
	}

	@GetMapping("/project/{projectId}/issues/{issueId}/timelog/{timeLogId}/update")
	public String updateForm(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@PathVariable("timeLogId") String timeLogId,
			@RequestParam(value = "issueSearchKeyword", required = false) String issueSearchKeyword,
			@RequestParam(value = "selectedIssueId", required = false) String selectedIssueId,
			Model model) {
		String loginUserId = getLoginUserId(userId);
		String checkedSelectedIssueId = selectedIssueId;
		if (model.containsAttribute("timelog") && model.asMap().get("timelog") instanceof TimelogVO) {
			checkedSelectedIssueId = ((TimelogVO) model.asMap().get("timelog")).getIssueId();
		}
		Map<String, Object> pageData = timelogService.getUpdatePageData(projectId, issueId, timeLogId, loginUserId,
				issueSearchKeyword, checkedSelectedIssueId);

		TimelogVO pageTimelog = (TimelogVO) pageData.get("timelog");
		TimelogVO formTimelog = model.containsAttribute("timelog") && model.asMap().get("timelog") instanceof TimelogVO
				? (TimelogVO) model.asMap().get("timelog")
				: pageTimelog;
		formTimelog.setOriginalIssueId(issueId);

		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("selectedProjectId", projectId);
		model.addAttribute("timelog", formTimelog);
		model.addAttribute("workTypeList", pageData.get("workTypeList"));
		model.addAttribute("issueList", pageData.get("issueList"));
		model.addAttribute("issueSearchKeyword", issueSearchKeyword);

		return "timelog/update";
	}

	@PostMapping("/project/{projectId}/issues/{issueId}/timelog/{timeLogId}/update")
	public String update(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@PathVariable("timeLogId") String timeLogId,
			TimelogVO timelog,
			RedirectAttributes redirectAttributes) {
		String loginUserId = getLoginUserId(userId);
		timelog.setProjectId(projectId);
		timelog.setOriginalIssueId(issueId);
		timelog.setTimeLogId(timeLogId);

		try {
			timelogService.updateTimelog(timelog, loginUserId);
			redirectAttributes.addFlashAttribute("message", "소요시간이 수정되었습니다.");
			return "redirect:/project/" + projectId + "/issues/" + timelog.getIssueId() + "/timelog/" + timeLogId;
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("timelog", timelog);
			return "redirect:/project/" + projectId + "/issues/" + issueId + "/timelog/" + timeLogId
					+ "/update" + queryParam("selectedIssueId", timelog.getIssueId());
		}
	}

	@PostMapping("/project/{projectId}/issues/{issueId}/timelog/{timeLogId}/delete")
	public String delete(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@PathVariable("timeLogId") String timeLogId,
			RedirectAttributes redirectAttributes) {
		String loginUserId = getLoginUserId(userId);

		try {
			timelogService.deleteTimelog(projectId, issueId, timeLogId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "소요시간이 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/project/" + projectId + "/issues/" + issueId + "#timeLogSection";
	}

	private TimelogVO getFormTimelog(Model model, String projectId, String issueId) {
		if (model.containsAttribute("timelog") && model.asMap().get("timelog") instanceof TimelogVO) {
			TimelogVO timelog = (TimelogVO) model.asMap().get("timelog");
			timelog.setProjectId(projectId);
			return timelog;
		}

		TimelogVO timelog = new TimelogVO();
		timelog.setProjectId(projectId);
		timelog.setIssueId(issueId);
		timelog.setWorkDate(LocalDate.now());
		return timelog;
	}

	private String getLoginUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}

		return userId;
	}

	private String queryParam(String name, String value) {
		if (value == null || value.trim().isEmpty()) {
			return "";
		}

		return "?" + name + "=" + value.trim();
	}

}
