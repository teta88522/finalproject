package com.pixcel.app.issues.web;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.issues.service.IssuesService;
import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.web.LoginRequiredException;

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

	@GetMapping("/project/{projectId}/issues/filter-options")
	@ResponseBody
	public Map<String, Object> issueListFilterOptions(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId) {
		String loginUserId = getLoginUserId(userId);
		return issuesService.getIssueListFilterData(projectId, loginUserId);
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

	@GetMapping("/project/{projectId}/issues/create/options")
	@ResponseBody
	public Map<String, Object> issueCreateExtraOptions(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId) {
		String loginUserId = getLoginUserId(userId);
		return issuesService.getIssueCreateExtraOptionData(projectId, loginUserId);
	}

	@PostMapping("/project/{projectId}/issues/create")
	public String issueCreate(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId, IssuesVO issue,
			@RequestParam(value = "submitAction", defaultValue = "next") String submitAction,
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

			if ("finish".equals(submitAction)) {
				return "redirect:/project/" + projectId + "/issues";
			}

			return "redirect:/project/" + projectId + "/issues/create";
		} catch (IllegalArgumentException e) {
			issue.setProjectId(projectId);
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issue", issue);
			return "redirect:/project/" + projectId + "/issues/create";
		}
	}

	// ==============================
	// 일감 상세조회 / 수정
	// ==============================

	@GetMapping("/project/{projectId}/issues/{issueId}")
	public String issueDetail(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			Model model) {

		String loginUserId = getLoginUserId(userId);
		addDetailModel(model, loginUserId, projectId, issueId);

		return "issues/detail";
	}

	@PostMapping("/project/{projectId}/issues/{issueId}/update")
	public String issueUpdate(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			IssuesVO issue,
			RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);
		issue.setProjectId(projectId);
		issue.setIssueId(issueId);

		try {
			issuesService.updateIssue(issue, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감을 수정했습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("issueForm", issue);
		}

		return "redirect:/project/" + projectId + "/issues/" + issueId;
	}

	@GetMapping("/project/{projectId}/issues/{issueId}/files")
	@ResponseBody
	public List<Map<String, Object>> issueFileList(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId) {

		String loginUserId = getLoginUserId(userId);
		issuesService.validateIssueAccess(projectId, issueId, loginUserId);

		List<FileVO> fileList = fileService.selectAll(issueId);

		if (fileList == null) {
			return java.util.Collections.emptyList();
		}

		return fileList.stream()
				.map(this::toIssueFileMap)
				.toList();
	}

	@GetMapping("/project/{projectId}/issues/{issueId}/files/download")
	public void issueFileDownloadAll(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			HttpServletResponse response) throws IOException {

		String loginUserId = getLoginUserId(userId);
		issuesService.validateIssueAccess(projectId, issueId, loginUserId);
		fileService.downloadAll(issueId, response, loginUserId);
	}

	@GetMapping("/project/{projectId}/issues/{issueId}/files/{fileId}/download")
	public void issueFileDownloadOne(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@PathVariable("fileId") String fileId,
			HttpServletResponse response) throws IOException {

		String loginUserId = getLoginUserId(userId);
		issuesService.validateIssueAccess(projectId, issueId, loginUserId);
		if (!isIssueFile(issueId, fileId)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		fileService.downloadOne(fileId, response, loginUserId);
	}
	
	@PostMapping("/project/{projectId}/issues/{issueId}/delete")
	public String issueDelete(@CookieValue(value = "userId", required = false) String userId,
			@PathVariable("projectId") String projectId,
			@PathVariable("issueId") String issueId,
			@RequestParam(value = "redirectTo", required = false) String redirectTo,
			RedirectAttributes redirectAttributes) {

		String loginUserId = getLoginUserId(userId);

		try {
			issuesService.deleteIssue(projectId, issueId, loginUserId);
			redirectAttributes.addFlashAttribute("message", "일감이 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			if ("detail".equals(redirectTo)) {
				return "redirect:/project/" + projectId + "/issues/" + issueId;
			}
		}

		return "redirect:/project/" + projectId + "/issues";
	}

	// ==============================
	// 일감 전체조회 화면 데이터
	// ==============================

	private void addListModel(Model model, String userId, String projectId, IssuesVO searchVO) {
		if (searchVO == null) {
			searchVO = new IssuesVO();
		}

		Map<String, Object> pageData = issuesService.getIssueListPageData(projectId, searchVO, userId);

		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);

		model.addAttribute("searchVO", searchVO);
		model.addAttribute("issueList", pageData.get("issueList"));

		model.addAttribute("issueTypeList", pageData.get("issueTypeList"));
		model.addAttribute("issueStatusList", pageData.get("issueStatusList"));
		model.addAttribute("versionList", pageData.get("versionList"));
		model.addAttribute("priorityList", pageData.get("priorityList"));
		model.addAttribute("assigneeList", pageData.get("assigneeList"));

		model.addAttribute("canCreateIssue", pageData.get("canCreateIssue"));
		model.addAttribute("canCreateMilestone", pageData.get("canCreateMilestone"));
	}

	private void addDetailModel(Model model, String userId, String projectId, String issueId) {
		Map<String, Object> pageData = issuesService.getIssueDetailPageData(projectId, issueId, userId);
		IssuesVO issue = (IssuesVO) pageData.get("issue");

		if (model.containsAttribute("issueForm") && model.asMap().get("issueForm") instanceof IssuesVO) {
			IssuesVO issueForm = (IssuesVO) model.asMap().get("issueForm");
			issueForm.setProjectId(projectId);
			issueForm.setIssueId(issueId);
			issueForm.setIssueNo(issue.getIssueNo());
			issueForm.setDisplayIssueNo(issue.getDisplayIssueNo());
			issueForm.setProjectName(issue.getProjectName());
			issueForm.setIssueTypeId(issue.getIssueTypeId());
			issueForm.setIssueTypeName(issue.getIssueTypeName());
			issueForm.setVersionId(issue.getVersionId());
			issueForm.setVersionName(issue.getVersionName());
			issueForm.setAuthorId(issue.getAuthorId());
			issueForm.setAuthorName(issue.getAuthorName());
			issueForm.setCreatedAt(issue.getCreatedAt());
			issueForm.setUpdatedAt(issue.getUpdatedAt());
			issue = issueForm;
		}

		model.addAttribute("projectInfo", issue);
		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);
		model.addAttribute("issue", issue);
		model.addAttribute("fieldSettingList", pageData.get("fieldSettingList"));
		model.addAttribute("availableStatusList", pageData.get("availableStatusList"));
		model.addAttribute("priorityList", pageData.get("priorityList"));
		model.addAttribute("assigneeList", pageData.get("assigneeList"));
		model.addAttribute("milestoneList", pageData.get("milestoneList"));
		model.addAttribute("parentIssueList", pageData.get("parentIssueList"));
		model.addAttribute("historyList", pageData.get("historyList"));
		model.addAttribute("canUpdateIssue", pageData.get("canUpdateIssue"));
		model.addAttribute("canDeleteIssue", pageData.get("canDeleteIssue"));
	}
	// ==============================
	// 일감 생성 화면 데이터
	// ==============================

	private void addCreateFormModel(Model model, String userId, String projectId) {
		Map<String, Object> pageData = issuesService.getIssueCreatePageData(projectId, userId);
		List<IssuesVO> issueTypeList = (List<IssuesVO>) pageData.get("issueTypeList");
		List<IssuesVO> versionList = (List<IssuesVO>) pageData.get("versionList");
		List<IssuesVO> priorityList = (List<IssuesVO>) pageData.get("priorityList");
		model.addAttribute("projectInfo", pageData.get("projectInfo"));
		model.addAttribute("projectId", projectId);
		model.addAttribute("selectedProjectId", projectId);

		model.addAttribute("issueTypeList", issueTypeList);
		model.addAttribute("fieldSettingList", pageData.get("fieldSettingList"));
		model.addAttribute("versionList", versionList);
		model.addAttribute("milestoneList", pageData.get("milestoneList"));
		model.addAttribute("priorityList", priorityList);
		model.addAttribute("assigneeList", pageData.get("assigneeList"));
		model.addAttribute("parentIssueList", pageData.get("parentIssueList"));
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
		uploadDTO.setFileCode("f008");
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

	private boolean isIssueFile(String issueId, String fileId) {
		List<FileVO> fileList = fileService.selectAll(issueId);

		if (fileList == null || fileList.isEmpty()) {
			return false;
		}

		return fileList.stream().anyMatch(file -> fileId.equals(file.getFileId()));
	}

	private Map<String, Object> toIssueFileMap(FileVO file) {
		Map<String, Object> fileMap = new HashMap<>();

		fileMap.put("fileId", file.getFileId());
		fileMap.put("originalName", file.getOriginalName());
		fileMap.put("fileVersion", file.getFileVersion());
		fileMap.put("fileSize", file.getFileSize());

		return fileMap;
	}

	private String getLoginUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new LoginRequiredException();
		}
		return userId;
	}
}
