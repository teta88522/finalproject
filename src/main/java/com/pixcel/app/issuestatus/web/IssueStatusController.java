package com.pixcel.app.issuestatus.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.issuestatus.service.IssueStatusService;
import com.pixcel.app.issuestatus.service.IssueStatusVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IssueStatusController {

    private final IssueStatusService issueStatusService;

    private static final String TEMP_LOGIN_USER_ID = "USERS_0001";

    // 일감 상태 목록 화면으로 이동한다.
    @GetMapping("/issuestatus/list")
    public String issueStatusList(Model model) {

        String userId = getLoginUserId();

        model.addAttribute("issueStatusList", issueStatusService.getIssueStatusList(userId));

        return "issuestatus/list";
    }

    // 일감 상태 등록 화면으로 이동한다.
    @GetMapping("/issuestatus/create")
    public String issueStatusCreateForm(Model model) {

        if (!model.containsAttribute("issueStatus")) {
            model.addAttribute("issueStatus", new IssueStatusVO());
        }

        return "issuestatus/create";
    }

    // 신규 일감 상태를 등록한다.
    @PostMapping("/issuestatus/create")
    public String issueStatusCreate(IssueStatusVO issueStatus,
                                    RedirectAttributes redirectAttributes) {

        String userId = getLoginUserId();

        try {
            issueStatusService.createIssueStatus(issueStatus, userId);
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
    public String issueStatusUpdateForm(@RequestParam("issueStatusId") String issueStatusId,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {

        String userId = getLoginUserId();

        try {
            model.addAttribute("issueStatus", issueStatusService.getIssueStatusDetail(issueStatusId, userId));

            return "issuestatus/update";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/issuestatus/list";
        }
    }

    // 기존 일감 상태 정보를 수정한다.
    @PostMapping("/issuestatus/update")
    public String issueStatusUpdate(IssueStatusVO issueStatus,
                                    RedirectAttributes redirectAttributes) {

        String userId = getLoginUserId();

        try {
            issueStatusService.modifyIssueStatus(issueStatus, userId);
            redirectAttributes.addFlashAttribute("message", "일감 상태가 수정되었습니다.");

            return "redirect:/issuestatus/list";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/issuestatus/update?issueStatusId=" + issueStatus.getIssueStatusId();
        }
    }

    // 사용 중이지 않은 일감 상태를 삭제한다.
    @PostMapping("/issuestatus/delete")
    public String issueStatusDelete(@RequestParam("issueStatusId") String issueStatusId,
                                    RedirectAttributes redirectAttributes) {

        String userId = getLoginUserId();

        try {
            issueStatusService.removeIssueStatus(issueStatusId, userId);
            redirectAttributes.addFlashAttribute("message", "일감 상태가 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/issuestatus/list";
    }

    // 현재 로그인 사용자 ID를 반환한다.
    private String getLoginUserId() {
        return TEMP_LOGIN_USER_ID;
    }
}