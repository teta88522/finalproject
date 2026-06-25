package com.pixcel.app.codevalue.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.codevalue.service.CodeValueService;
import com.pixcel.app.codevalue.service.CodeValueVO;
import com.pixcel.app.web.LoginRequiredException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CodeValueController {

	private final CodeValueService codeValueService;

	// 코드값 목록 화면으로 이동한다.
	@GetMapping("/codevalue/list")
	public String codeValueList(@CookieValue(value = "userId", required = false) String userId,
								CodeValueVO searchVO,
								Model model) {

		String loginUserId = getLoginUserId(userId);

		searchVO.setUserId(loginUserId);

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("groupNameMap", getGroupNameMap());
		model.addAttribute("codeValueGroupMap", codeValueService.getCodeValueSearchGroupMap(searchVO));

		return "codevalue/list";
	}

	// 코드값 등록 화면으로 이동한다.
	@GetMapping("/codevalue/create")
	public String codeValueCreateForm(@CookieValue(value = "userId", required = false) String userId,
									  @RequestParam(value = "settingGroupName", required = false) String settingGroupName,
									  Model model) {

		getLoginUserId(userId);

		if (!model.containsAttribute("codeValue")) {
			CodeValueVO codeValue = new CodeValueVO();
			codeValue.setSettingGroupName(settingGroupName);
			model.addAttribute("codeValue", codeValue);
		}

		model.addAttribute("groupNameMap", getGroupNameMap());

		return "codevalue/create";
	}

	// 신규 코드값을 등록한다.
	@PostMapping("/codevalue/create")
	public String codeValueCreate(@CookieValue(value = "userId", required = false) String userId,
								  CodeValueVO codeValue,
								  RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			codeValueService.createCodeValue(codeValue, loginUserId);
			redirectAttributes.addFlashAttribute("message", "코드값이 등록되었습니다.");

			return "redirect:/codevalue/list";

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("codeValue", codeValue);

			return "redirect:/codevalue/create?settingGroupName=" + codeValue.getSettingGroupName();
		}
	}

	// 코드값을 기본값으로 설정한다.
	@PostMapping({"/codevalue/default", "/codevalue/default/set"})
	public String codeValueDefault(@CookieValue(value = "userId", required = false) String userId,
								   @RequestParam("settingCodeId") String settingCodeId,
								   RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			codeValueService.setDefaultCodeValue(settingCodeId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "기본값으로 설정되었습니다.");

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/codevalue/list";
	}

	// 코드값 기본값 설정을 취소한다.
	@PostMapping("/codevalue/default/cancel")
	public String codeValueDefaultCancel(@CookieValue(value = "userId", required = false) String userId,
										 @RequestParam("settingCodeId") String settingCodeId,
										 RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			codeValueService.cancelDefaultCodeValue(settingCodeId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "기본값 설정이 취소되었습니다.");

		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/codevalue/list";
	}

	// 코드값 삭제 전 대체값 선택 필요 여부를 확인한다.
	@PostMapping("/codevalue/delete")
	public String codeValueDelete(@CookieValue(value = "userId", required = false) String userId,
								  @RequestParam("settingCodeId") String settingCodeId,
								  RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			boolean replacementRequired = codeValueService.isReplacementRequired(settingCodeId, loginUserId);

			if (replacementRequired) {
				redirectAttributes.addFlashAttribute("errorMessage", "삭제 전 대체 코드값을 선택해야 합니다.");

				return "redirect:/codevalue/delete/replace?settingCodeId=" + settingCodeId;
			}

			codeValueService.removeCodeValue(settingCodeId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "코드값이 삭제되었습니다.");

		} catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/codevalue/list";
	}

	// 코드값 대체 삭제 화면으로 이동한다.
	@GetMapping("/codevalue/delete/replace")
	public String codeValueReplaceDeleteForm(@CookieValue(value = "userId", required = false) String userId,
											 @RequestParam("settingCodeId") String settingCodeId,
											 Model model,
											 RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			CodeValueVO deleteTarget = codeValueService.getCodeValueDetail(settingCodeId, loginUserId);

			model.addAttribute("deleteTarget", deleteTarget);
			model.addAttribute("replacementList", codeValueService.getReplacementCodeValueList(settingCodeId, loginUserId));
			model.addAttribute("groupNameMap", getGroupNameMap());

			return "codevalue/replace-delete";

		} catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/codevalue/list";
		}
	}

	// 대체 코드값으로 변경 후 코드값을 삭제한다.
	@PostMapping({"/codevalue/delete/replacement", "/codevalue/delete/replace"})
	public String codeValueDeleteWithReplacement(@CookieValue(value = "userId", required = false) String userId,
												 @RequestParam("deleteSettingCodeId") String deleteSettingCodeId,
												 @RequestParam("replaceSettingCodeId") String replaceSettingCodeId,
												 RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			codeValueService.removeCodeValueWithReplacement(deleteSettingCodeId, replaceSettingCodeId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "대체 코드값으로 변경 후 삭제되었습니다.");

			return "redirect:/codevalue/list";

		} catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/codevalue/delete/replace?settingCodeId=" + deleteSettingCodeId;
		}
	}

	// 현재 로그인 사용자 ID를 반환한다.
	private String getLoginUserId(String userId) {

		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}

		return userId;
	}

	// 코드값 그룹명을 화면 표시용 이름으로 반환한다.
	private Map<String, String> getGroupNameMap() {

		Map<String, String> groupNameMap = new LinkedHashMap<>();

		groupNameMap.put("g001", "작업분류");
		groupNameMap.put("g002", "일감 우선순위");
		groupNameMap.put("g003", "문서유형");

		return groupNameMap;
	}
}
