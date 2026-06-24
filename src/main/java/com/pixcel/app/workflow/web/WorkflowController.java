package com.pixcel.app.workflow.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.workflow.service.WorkflowService;
import com.pixcel.app.workflow.service.WorkflowVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    // 상태전환 설정 화면으로 이동한다.
    @GetMapping("/workflow/transition")
    public String workflowTransition(@CookieValue(value = "userId", required = false) String userId,
                                     WorkflowVO searchVO,
                                     Model model) {

        String loginUserId = getLoginUserId(userId);

        List<WorkflowVO> issueTypeList = workflowService.getIssueTypeList(loginUserId);
        List<WorkflowVO> roleList = workflowService.getRoleList(loginUserId);

        setDefaultSearchCondition(searchVO, issueTypeList, roleList);

        searchVO.setUserId(loginUserId);

        // 세로축 현재 상태: 항상 전체 상태 조회
        WorkflowVO fromStatusSearchVO = new WorkflowVO();
        fromStatusSearchVO.setUserId(loginUserId);

        List<WorkflowVO> fromStatusList = workflowService.getIssueStatusList(fromStatusSearchVO);

        // 가로축 다음 상태: 완료여부 조건 적용
        WorkflowVO toStatusSearchVO = new WorkflowVO();
        toStatusSearchVO.setUserId(loginUserId);
        toStatusSearchVO.setClosedYn(searchVO.getClosedYn());

        List<WorkflowVO> toStatusList = workflowService.getIssueStatusList(toStatusSearchVO);

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

        return "workflow/transition";
    }

    // 상태전환 설정을 저장한다.
    @PostMapping("/workflow/transition/save")
    public String saveWorkflowTransition(@CookieValue(value = "userId", required = false) String userId,
                                         WorkflowVO workflowVO,
                                         RedirectAttributes redirectAttributes) {

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

    // 최초 진입 시 기본 조회조건을 설정한다.
    private void setDefaultSearchCondition(WorkflowVO searchVO,
                                           List<WorkflowVO> issueTypeList,
                                           List<WorkflowVO> roleList) {

        if (isBlank(searchVO.getRoleId()) && roleList != null && !roleList.isEmpty()) {
            searchVO.setRoleId(roleList.get(0).getRoleId());
        }

        if (isBlank(searchVO.getIssueTypeId()) && issueTypeList != null && !issueTypeList.isEmpty()) {
            searchVO.setIssueTypeId(issueTypeList.get(0).getIssueTypeId());
        }

        if (isBlank(searchVO.getApplyTargetCode())) {
            searchVO.setApplyTargetCode("j003");
        }
    }

    // 조회 가능한 조건이 모두 있는지 확인한다.
    private boolean hasSearchCondition(WorkflowVO searchVO) {

        return !isBlank(searchVO.getIssueTypeId())
                && !isBlank(searchVO.getRoleId())
                && !isBlank(searchVO.getApplyTargetCode());
    }

    // 로그인 사용자 ID를 확인한다.
    private String getLoginUserId(String userId) {

        if (isBlank(userId)) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        return userId;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}