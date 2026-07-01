package com.pixcel.app.sourcerepository.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.sourcerepository.service.sourcerepositoryPageVO;
import com.pixcel.app.sourcerepository.service.sourcerepositoryService;
import com.pixcel.app.sourcerepository.service.sourcerepositoryVO;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

@AllProjectController
@Controller
@RequestMapping("/sourcerepository")
@RequiredArgsConstructor
public class SourcerepositoryController {
	private final sourcerepositoryService service;
	private final ProjectService projectService;

	// ==============================
	// FUR-016-00: 저장소 초기화면 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository)
	// ==============================

	@GetMapping
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String index(@PathVariable String projectId, Model model) {
		try {
			model.addAttribute("projectId", projectId);
			return "sourcerepository/sourcerepositoryIndex";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "저장소 정보 조회 실패: " + e.getMessage());
			return "sourcerepository/sourcerepositoryIndex";
		}
	}
	
	@PostMapping("/validate-url")
	@PreAuthorize("hasAuthority('저장소_관리')")
	@ResponseBody
	public Map<String, Object> validateGitHubUrl(@RequestParam String gitHubUrl) {
	    try {
	        Map<String, Object> result = service.getGitHubBranches(gitHubUrl);
	        return result;
	    } catch (Exception e) {
	        Map<String, Object> error = new java.util.HashMap<>();
	        error.put("success", false);
	        error.put("error", "저장소를 찾을 수 없습니다: " + e.getMessage());
	        return error;
	    }
	}

	// ==============================
	// FUR-016-00: GitHub 동기화 - 모달 POST 처리 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/sync-modal)
	// ==============================

	@PostMapping("/sync-modal")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String syncModal(
			@PathVariable String projectId,
			@RequestParam String gitHubUrl,
			@RequestParam String branch,
			RedirectAttributes rttr) {

		try {
			// ✨ 1단계: GitHub URL을 DB에 저장
			service.saveGitHubUrl(projectId, gitHubUrl);
			
			// ✨ 2단계: Commit 동기화
			service.syncSourcerepositoryFromGitHub(projectId, branch);

			rttr.addFlashAttribute("successMessage", "GitHub 저장소가 연동되고 Commit이 동기화되었습니다.");
			return "redirect:/project/" + projectId + "/sourcerepository";

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "동기화에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}

	// ==============================
	// FUR-016-01: 파일 목록 조회 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/files)
	// ==============================

	@GetMapping("/files")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String fileList(
	        @PathVariable String projectId,
	        @RequestParam(value = "branch", required = false) String branch,
	        @RequestParam(value = "path", required = false) String path,
	        Model model) {

	    try {
	        // 1. 여기서 gitUrl을 먼저 선언하고 프로젝트 정보를 가져옵니다
	        ProjectVO project = projectService.selectProjectDetail(projectId);
	        String gitUrl = project.getGitUrl();
	        
	        if (gitUrl == null || gitUrl.isEmpty()) {
	            throw new RuntimeException("GitHub URL이 설정되지 않았습니다");
	        }

	        // 2. branch가 없을 때 기본 브랜치 설정
	        if (branch == null || branch.isEmpty()) {
	            Map<String, Object> branchInfo = service.getGitHubBranches(gitUrl);
	            branch = (String) branchInfo.get("defaultBranch");
	        }

	        // 3. 선언된 gitUrl 변수를 사용[cite: 7]
	        List<Map<String, Object>> files = service.getRepositoryFiles(projectId, gitUrl, path, branch);
	        
	        model.addAttribute("projectId", projectId);
	        model.addAttribute("files", files);
	        model.addAttribute("branch", branch);
	        model.addAttribute("currentPath", path);

	        return "sourcerepository/sourcerepositoryFileList";

	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "파일 목록 조회 실패: " + e.getMessage());
	        return "sourcerepository/sourcerepositoryIndex";
	    }
	}

	// ==============================
	// FUR-016-02: 파일 상세 조회 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/file/{path})
	// ==============================

	@GetMapping("/file/{path:.*}")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String fileDetail(
			@PathVariable String projectId,
			@PathVariable String path,
			@RequestParam(required = false) String branch,
			RedirectAttributes rttr,
			Model model) {

		try {
			// ✨ branch가 없으면 getGitHubBranches()로 기본값 조회
			if (branch == null || branch.isEmpty()) {
				ProjectVO project = projectService.selectProjectDetail(projectId);
				String gitUrl = project.getGitUrl();
				
				if (gitUrl == null || gitUrl.isEmpty()) {
					throw new RuntimeException("GitHub URL이 설정되지 않았습니다");
				}
				
				// getGitHubBranches()는 이미 기본 branch를 반환함
				Map<String, Object> branchInfo = service.getGitHubBranches(gitUrl);
				branch = (String) branchInfo.get("defaultBranch");
			}

			String fileContent = service.getFileContent(projectId, path, branch);

			model.addAttribute("projectId", projectId);
			model.addAttribute("fileName", path.substring(path.lastIndexOf('/') + 1));
			model.addAttribute("filePath", path);
			model.addAttribute("fileContent", fileContent);
			model.addAttribute("branch", branch);
			model.addAttribute("fileSize", "N/A");
			model.addAttribute("lastModified", java.time.LocalDateTime.now());

			return "sourcerepository/sourcerepositoryFileDetail";

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "파일 상세 조회에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository/files";
		}
	}

	// ==============================
	// FUR-016-03: Commit 목록 조회 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/commits?branch=master&page=1)
	// ==============================

	@GetMapping("/commits")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String commits(
			@PathVariable String projectId,
			@RequestParam(required = false) String branch,
			@RequestParam(defaultValue = "1") int page,
			RedirectAttributes rttr,
			Model model) {

		try {
			// 1. DB에서 Commit 조회 (LEFT JOIN으로 ISSUE 정보 포함)
			sourcerepositoryPageVO<sourcerepositoryVO> commitPage = 
				service.getSourcerepositoryCommits(projectId, branch, page, 20);

			// 2. 모델에 데이터 추가
			model.addAttribute("commits", commitPage.getContent());
			model.addAttribute("currentPage", commitPage.getCurrentPage());
			model.addAttribute("totalPages", commitPage.getTotalPages());
			model.addAttribute("totalElements", commitPage.getTotalElements());
			model.addAttribute("hasNextPage", commitPage.hasNextPage());
			model.addAttribute("hasPreviousPage", commitPage.hasPreviousPage());
			model.addAttribute("branch", branch);
			model.addAttribute("projectId", projectId);

			// 3. 페이지 네비게이션 계산
			int startPage = ((page - 1) / 5) * 5 + 1;
			int endPage = Math.min(startPage + 4, commitPage.getTotalPages());
			
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);

			return "sourcerepository/sourcerepositoryCommitList";

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "Commit 조회에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}

	// ==============================
	// Commit 상세보기 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/commit/{sha}?branch=master)
	// ==============================

	@GetMapping("/commit/{sha}")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String commitDetail(
			@PathVariable String projectId,
			@PathVariable String sha,
			@RequestParam(required = false) String branch,
			RedirectAttributes rttr,
			Model model) {

		try {
			// 1. DB에서 Commit 메타정보 조회
			sourcerepositoryVO commitLog = service.getSourcerepositoryCommit(sha);

			model.addAttribute("commit", commitLog);
			model.addAttribute("projectId", projectId);
			model.addAttribute("branch", branch);

			return "sourcerepository/sourcerepositoryCommitDetail";

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "Commit 상세 조회에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}

	// ==============================
	// GitHub 동기화 (구 방식 - 폼 POST) (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/sync?branch=master)
	// ==============================

	@PostMapping("/sync")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String syncCommits(
			@PathVariable String projectId,
			@RequestParam(required = false) String branch,
			RedirectAttributes rttr) {

		try {
			// GitHub에서 Commit 조회 & DB 저장
			service.syncSourcerepositoryFromGitHub(projectId, branch);

			rttr.addFlashAttribute("successMessage", "Commit이 동기화되었습니다.");
			return "redirect:/project/" + projectId + "/sourcerepository/commits?branch=" + branch;

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "동기화에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}
}