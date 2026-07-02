package com.pixcel.app.sourcerepository.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

			// ✅ 저장된 GitHub URL / 첫 브랜치를 조회해서 모달 프리필 + 정보 카드 + 전체동기화 버튼 노출에 사용
			ProjectVO project = projectService.selectProjectDetail(projectId);
			String savedGitUrl = project != null ? project.getGitUrl() : null;
			model.addAttribute("savedGitUrl", savedGitUrl);

			if (savedGitUrl != null && !savedGitUrl.isEmpty()) {
				String savedBranch = service.getFirstBranchByProjectId(projectId);
				model.addAttribute("savedBranch", savedBranch);
			}

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
	public String syncModal(@PathVariable String projectId, @RequestParam String gitHubUrl, @RequestParam String branch,
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
	// ✅ 전체 브랜치 동기화 (저장소에 등록된 모든 브랜치를 한 번에 동기화)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/sync-all)
	// ==============================

	@PostMapping("/sync-all")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String syncAllBranches(@PathVariable String projectId, RedirectAttributes rttr) {
		try {
			Map<String, Object> result = service.syncAllBranchesFromGitHub(projectId);

			@SuppressWarnings("unchecked")
			List<String> succeeded = (List<String>) result.get("succeeded");
			@SuppressWarnings("unchecked")
			List<String> failed = (List<String>) result.get("failed");

			StringBuilder msg = new StringBuilder();
			msg.append(succeeded.size()).append("개 브랜치 동기화 완료");
			if (!succeeded.isEmpty()) {
				msg.append(" (").append(String.join(", ", succeeded)).append(")");
			}
			if (!failed.isEmpty()) {
				msg.append(" / 실패 ").append(failed.size()).append("개: ").append(String.join(", ", failed));
			}

			// 하나라도 실패했으면 errorMessage로, 전부 성공이면 successMessage로 표시
			rttr.addFlashAttribute(failed.isEmpty() ? "successMessage" : "errorMessage", msg.toString());
			return "redirect:/project/" + projectId + "/sourcerepository";

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "전체 브랜치 동기화에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}

	// ==============================
	// FUR-016-01: 파일 목록 조회 (관리 권한 필요)
	// (경로 : localhost:8080/project/{projectId}/sourcerepository/files)
	// ==============================

	@GetMapping("/files")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String fileList(@PathVariable String projectId,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "path", required = false) String path, Model model) {

		try {
			// 1. 프로젝트 정보 조회
			ProjectVO project = projectService.selectProjectDetail(projectId);
			String gitUrl = project.getGitUrl();

			if (gitUrl == null || gitUrl.isEmpty()) {
				throw new RuntimeException("GitHub URL이 설정되지 않았습니다");
			}

			// 2. ✅ branch가 없으면 DB에서 먼저 조회 (동적 처리)
			if (branch == null || branch.isEmpty()) {
				branch = service.getFirstBranchByProjectId(projectId);

				// DB에 데이터가 없으면 GitHub에서 조회
				if (branch == null || branch.isEmpty()) {
					Map<String, Object> branchInfo = service.getGitHubBranches(gitUrl);
					branch = (String) branchInfo.get("defaultBranch");
				}
			}

			// 3. 파일 목록 조회
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
	public String fileDetail(@PathVariable String projectId, @PathVariable String path,
			@RequestParam(required = false) String branch, RedirectAttributes rttr, Model model) {

		try {
			// ✅ branch가 없으면 DB에서 동적으로 조회
			if (branch == null || branch.isEmpty()) {
				branch = service.getFirstBranchByProjectId(projectId);
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
	// (경로 :
	// localhost:8080/project/{projectId}/sourcerepository/commits?branch=master&page=1)
	// ==============================

	@GetMapping("/commits")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String commits(@PathVariable String projectId, @RequestParam(required = false) String branch,
			@RequestParam(defaultValue = "1") int page, RedirectAttributes rttr, Model model) {

		try {
			// ✅ branch가 없으면 DB에서 첫 번째 branch 조회 (동적)
			if (branch == null || branch.isEmpty()) {
				branch = service.getFirstBranchByProjectId(projectId);
			}

			// DB에서 Commit 조회
			sourcerepositoryPageVO<sourcerepositoryVO> commitPage = service.getSourcerepositoryCommits(projectId,
					branch, page, 20);

			model.addAttribute("commits", commitPage.getContent());
			model.addAttribute("currentPage", commitPage.getCurrentPage());
			model.addAttribute("totalPages", commitPage.getTotalPages());
			model.addAttribute("totalElements", commitPage.getTotalElements());
			model.addAttribute("hasNextPage", commitPage.hasNextPage());
			model.addAttribute("hasPreviousPage", commitPage.hasPreviousPage());
			model.addAttribute("branch", branch);
			model.addAttribute("projectId", projectId);

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
	// (경로 :
	// localhost:8080/project/{projectId}/sourcerepository/commit/{sha}?branch=master)
	// ==============================

	@GetMapping("/commit/{sha}")
	@PreAuthorize("hasAuthority('저장소_관리')")
	public String commitDetail(@PathVariable String projectId, @PathVariable String sha,
			@RequestParam(required = false) String branch, RedirectAttributes rttr, Model model) {

		try {
			// 1. DB에서 Commit 메타정보 조회
			// 1. DB에서 Commit 메타정보 조회 (branch로 좁혀서 SHA 중복 문제 방지)
			sourcerepositoryVO commitLog = service.getSourcerepositoryCommit(sha, branch);

			// ✅ 조회 결과가 없어도 예외로 죽지 않고, 템플릿에서 안내 메시지를 보여주도록 그대로 전달
			if (commitLog == null) {
				System.err.println("⚠️ Commit 상세 조회 결과 없음: sha=" + sha + ", projectId=" + projectId);
			}

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
	public String syncCommits(@PathVariable String projectId, @RequestParam(required = false) String branch,
			RedirectAttributes rttr) {

		try {
			// ✅ branch가 없으면 DB에서 동적으로 조회
			if (branch == null || branch.isEmpty()) {
				branch = service.getFirstBranchByProjectId(projectId);
			}

			// GitHub에서 Commit 조회 & DB 저장
			service.syncSourcerepositoryFromGitHub(projectId, branch);

			rttr.addFlashAttribute("successMessage", "Commit이 동기화되었습니다.");
			return "redirect:/project/" + projectId + "/sourcerepository/commits?branch=" + branch;

		} catch (Exception e) {
			rttr.addFlashAttribute("errorMessage", "동기화에 실패했습니다: " + e.getMessage());
			return "redirect:/project/" + projectId + "/sourcerepository";
		}
	}

	// Branch 목록 API
	@GetMapping("/api/branches")
	@PreAuthorize("hasAuthority('저장소_관리')")
	@ResponseBody
	public List<String> getBranchList(@PathVariable String projectId) {
		try {
			// ✅ "동기화된 브랜치만" 보여주던 문제 수정: 항상 GitHub의 실제 브랜치 목록을 우선 사용.
			//    getGitHubBranches()는 60초 캐시가 있어 반복 호출해도 rate limit 걱정이 적음.
			//    이렇게 해야 아직 한 번도 동기화 안 한 브랜치도 드롭다운에 보이고, 파일 목록 조회(GitHub 직접 호출)가 가능함.
			ProjectVO project = projectService.selectProjectDetail(projectId);
			String gitUrl = project != null ? project.getGitUrl() : null;

			if (gitUrl != null && !gitUrl.isEmpty()) {
				try {
					Map<String, Object> branchInfo = service.getGitHubBranches(gitUrl);
					@SuppressWarnings("unchecked")
					List<Map<String, String>> githubBranches = (List<Map<String, String>>) branchInfo.get("branches");

					if (githubBranches != null && !githubBranches.isEmpty()) {
						return githubBranches.stream().map(b -> b.get("name")).sorted().collect(Collectors.toList());
					}
				} catch (Exception githubEx) {
					// GitHub 호출이 실패해도(rate limit 등) 아래에서 DB 브랜치로라도 폴백
					System.err.println("⚠️ GitHub 브랜치 조회 실패, DB 브랜치로 폴백: " + githubEx.getMessage());
				}
			}

			// ✅ 폴백: GitHub URL이 없거나 GitHub 호출이 실패한 경우, 그동안 동기화된 DB 브랜치라도 보여줌
			List<sourcerepositoryVO> commits = service.getSourcerepositoryCommitsByProjectId(projectId);
			return commits.stream().map(sourcerepositoryVO::getBranchName).distinct().sorted()
					.collect(Collectors.toList());

		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

}