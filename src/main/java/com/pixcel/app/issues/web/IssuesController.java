package com.pixcel.app.issues.web;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/issues")
	public String issues() {
		return "redirect:/project/list";
	}
	// ==============================
	// 일감 전체조회
	// ==============================

	@GetMapping("/project/{projectId}/issues")
	public String issueList(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId, IssuesVO searchVO, Model model) {

		String loginUserId = getLoginUserId(userId);

		addListModel(model, loginUserId, projectId, searchVO);

		return "issues/list";
	}

	// ==============================
	// 일감 생성
	// ==============================

	@GetMapping("/project/{projectId}/issues/create")
	public String issueCreateForm(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId, Model model) {
		String loginUserId = getLoginUserId(userId);
		addCreateFormModel(model, loginUserId, projectId);
		return "issues/create";
	}

	@PostMapping("/project/{projectId}/issues/create")
	public String issueCreate(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId, IssuesVO issue,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			RedirectAttributes redirectAttributes) {
		String loginUserId = getLoginUserId(userId);
		issue.setProjectId(projectId);
		try {
			issuesService.createIssue(issue, loginUserId);
			int selectedFileCount = countSelectedFiles(files);
			int uploadFileCount = uploadIssueFiles(issue, loginUserId, files);
			redirectAttributes.addFlashAttribute("message", "일감이 등록되었습니다.");
			redirectAttributes.addFlashAttribute("createdIssueId", issue.getDisplayIssueNo());

			if (selectedFileCount > uploadFileCount) {
				redirectAttributes.addFlashAttribute("fileWarning", "일감은 등록되었지만 일부 첨부파일을 저장하지 못했습니다.");
			}
			return "redirect:/project/" + projectId + "/issues/create";
		} catch (IllegalArgumentException e) {
			issue.setProjectId(projectId);
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issue", issue);
			return "redirect:/project/" + projectId + "/issues/create";
		}
	}
	
	@PostMapping("/project/{projectId}/issues/{issueId}/delete")
	public String issueDelete(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issuesService.deleteIssue(projectId, issueId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감이 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/project/" + projectId + "/issues";
	}

	// ==============================
	// 일감 전체조회 화면 데이터
	// ==============================

	private void addListModel(Model model, String userId, String projectId, IssuesVO searchVO) {
		IssuesVO projectInfo = issuesService.getProjectDetail(projectId, userId);

		model.addAttribute("projectInfo", projectInfo);
		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("issueList", issuesService.getIssueList(projectId, searchVO, userId));

		model.addAttribute("issueTypeList", issuesService.getIssueTypeList(projectId, userId));
		model.addAttribute("issueStatusList", issuesService.getIssueStatusList(projectId, userId));
		model.addAttribute("versionList", issuesService.getVersionList(projectId, userId));
		model.addAttribute("priorityList", issuesService.getPriorityList(projectId, userId));
		model.addAttribute("assigneeList", issuesService.getAssigneeList(projectId, userId));

		model.addAttribute("canCreateIssue", issuesService.canCreateIssue(projectId, userId));
		model.addAttribute("canCreateMilestone", issuesService.canCreateMilestone(projectId, userId));
		
		model.addAttribute("canDeleteIssue", issuesService.canDeleteIssue(projectId, userId));
	}
	// ==============================
	// 일감 생성 화면 데이터
	// ==============================

	private void addCreateFormModel(Model model, String userId, String projectId) {
		IssuesVO projectInfo = issuesService.getProjectDetailForCreate(projectId, userId);
		List<IssuesVO> issueTypeList = issuesService.getIssueTypeList(projectId, userId);
		List<IssuesVO> versionList = issuesService.getVersionList(projectId, userId);
		List<IssuesVO> priorityList = issuesService.getPriorityList(projectId, userId);
		model.addAttribute("projectInfo", projectInfo);
		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);

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
			return;
		}
		Object issueObject = model.asMap().get("issue");
		if (issueObject instanceof IssuesVO) {
			IssuesVO issue = (IssuesVO) issueObject;
			issue.setProjectId(projectId);
		}
	}

	// ==============================
	// 파일 업로드
	// ==============================

	private int uploadIssueFiles(IssuesVO issue, String userId, List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return 0;
		}

		List<MultipartFile> uploadFiles = files.stream().filter(file -> file != null && !file.isEmpty()).toList();

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

		return (int) files.stream().filter(file -> file != null && !file.isEmpty()).count();
	}

	private String getLoginUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		return userId;
	}
}
