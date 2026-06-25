package com.pixcel.app.issues.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.issues.service.IssuesService;
import com.pixcel.app.issues.service.IssuesVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IssuesController {

    private final IssuesService issuesService;
    private final FileService fileService;

    // 일감 메뉴 진입 시 오늘 구현 범위인 일감 등록 화면으로 이동한다.
    @GetMapping("/issues")
    public String issues(@CookieValue(value = "userId", required = false) String userId,
                         @RequestParam(value = "projectId", required = false) String projectId,
                         RedirectAttributes redirectAttributes) {

        String loginUserId = getLoginUserId(userId);
        String selectedProjectId = issuesService.getSelectedProjectId(projectId, loginUserId);

        if (selectedProjectId != null) {
            redirectAttributes.addAttribute("projectId", selectedProjectId);
        }

        return "redirect:/issues/create";
    }

    // 일감 등록 화면으로 이동한다.
    @GetMapping("/issues/create")
    public String issueCreateForm(@CookieValue(value = "userId", required = false) String userId,
                                  @RequestParam(value = "projectId", required = false) String projectId,
                                  Model model) {

        String loginUserId = getLoginUserId(userId);
        String selectedProjectId = issuesService.getSelectedProjectId(projectId, loginUserId);

        addCreateFormModel(model, loginUserId, selectedProjectId);

        return "issues/create";
    }

    // 신규 일감을 등록한다.
    @PostMapping("/issues/create")
    public String issueCreate(@CookieValue(value = "userId", required = false) String userId,
                              IssuesVO issue,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files,
                              RedirectAttributes redirectAttributes) {

        String loginUserId = getLoginUserId(userId);

        try {
            issuesService.createIssue(issue, loginUserId);
            int selectedFileCount = countSelectedFiles(files);
            int uploadFileCount = uploadIssueFiles(issue, loginUserId, files);

            redirectAttributes.addFlashAttribute("message", "일감이 등록되었습니다.");
            redirectAttributes.addFlashAttribute("createdIssueId", issue.getIssueId());
            if (selectedFileCount > uploadFileCount) {
                redirectAttributes.addFlashAttribute(
                        "fileWarning",
                        "일감은 등록되었지만 일부 첨부파일을 저장하지 못했습니다."
                );
            }

            redirectAttributes.addAttribute("projectId", issue.getProjectId());
            return "redirect:/issues/create";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("issue", issue);
            redirectAttributes.addAttribute("projectId", issue.getProjectId());

            return "redirect:/issues/create";
        }
    }

    // 기존 file 모듈은 건드리지 않고, issues 전용 commonfile 경로로 일감 첨부파일을 연결한다.
    private int uploadIssueFiles(IssuesVO issue, String userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }

        List<MultipartFile> uploadFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (uploadFiles.isEmpty()) {
            return 0;
        }

        FileDTO uploadDTO = new FileDTO();
        uploadDTO.setProjectId(issue.getProjectId());
        uploadDTO.setVersionId(issue.getVersionId());
        uploadDTO.setFileCode("f001");
        uploadDTO.setUploadUserId(userId);
        uploadDTO.setConnectAddress(issue.getIssueId());

        return fileService.uploadFile(uploadFiles, uploadDTO);
    }

    private int countSelectedFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }

        return (int) files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();
    }

    // 생성 화면에 필요한 목록 데이터를 담는다.
    private void addCreateFormModel(Model model, String userId, String projectId) {
        List<IssuesVO> projectList = issuesService.getProjectList(userId);

        model.addAttribute("projectList", projectList);
        model.addAttribute("projectId", projectId);
        model.addAttribute("selectedProjectId", projectId);

        if (projectId == null) {
            model.addAttribute("issueTypeList", List.of());
            model.addAttribute("fieldSettingList", List.of());
            model.addAttribute("versionList", List.of());
            model.addAttribute("milestoneList", List.of());
            model.addAttribute("priorityList", List.of());
            model.addAttribute("assigneeList", List.of());
            model.addAttribute("parentIssueList", List.of());

            addEmptyIssueModel(model);

            return;
        }

        List<IssuesVO> issueTypeList = issuesService.getIssueTypeList(projectId, userId);
        List<IssuesVO> versionList = issuesService.getVersionList(projectId, userId);
        List<IssuesVO> priorityList = issuesService.getPriorityList(projectId, userId);

        model.addAttribute("issueTypeList", issueTypeList);
        model.addAttribute("fieldSettingList", issuesService.getFieldSettingList(projectId, userId));
        model.addAttribute("versionList", versionList);
        model.addAttribute("milestoneList", issuesService.getMilestoneList(projectId, userId));
        model.addAttribute("priorityList", priorityList);
        model.addAttribute("assigneeList", issuesService.getAssigneeList(projectId, userId));
        model.addAttribute("parentIssueList", issuesService.getParentIssueList(projectId, userId));

        if (!model.containsAttribute("issue")) {
            IssuesVO issue = new IssuesVO();
            issue.setProjectId(projectId);
            issue.setProgressRate(0);

            if (!issueTypeList.isEmpty()) {
                issue.setIssueTypeId(issueTypeList.get(0).getIssueTypeId());
            }

            if (!versionList.isEmpty()) {
                issue.setVersionId(versionList.get(0).getVersionId());
            }

            for (IssuesVO priority : priorityList) {
                if ("Y".equals(priority.getDefaultYn())) {
                    issue.setSettingCodeId(priority.getSettingCodeId());
                    break;
                }
            }

            model.addAttribute("issue", issue);
        }
    }

    private void addEmptyIssueModel(Model model) {
        if (!model.containsAttribute("issue")) {
            IssuesVO issue = new IssuesVO();
            issue.setProgressRate(0);
            model.addAttribute("issue", issue);
        }
    }

    // 현재 로그인 사용자 ID를 반환한다.
    private String getLoginUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return userId;
    }
}
