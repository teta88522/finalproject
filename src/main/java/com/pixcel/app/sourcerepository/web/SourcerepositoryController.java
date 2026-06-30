package com.pixcel.app.sourcerepository.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.sourcerepository.service.sourcerepositoryPageVO;
import com.pixcel.app.sourcerepository.service.sourcerepositoryService;
import com.pixcel.app.sourcerepository.service.sourcerepositoryVO;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

@AllProjectController
@Controller
@RequestMapping("/sourcerepository")
@RequiredArgsConstructor
public class sourcerepositoryController {
	private final sourcerepositoryService service;

	// ==============================
	// 저장소 초기화면 (경로 : localhost:8080/project/{projectId}/sourcerepository)
	// ==============================

	@GetMapping
	public String index(@PathVariable String projectId, Model model) {
		model.addAttribute("projectId", projectId);
		return "sourcerepository/sourcerepositoryIndex";
	}

	// ==============================
	// Commit 목록 조회 (페이지네이션), (경로 : localhost:8080/project/{projectId}/sourcerepository/commits?branch=master&page=1)
	// ==============================

	@GetMapping("/commits")
	public String commits(
			@PathVariable String projectId,
			@RequestParam(defaultValue = "master") String branch,
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
	// Commit 상세보기 (Diff), (경로 : localhost:8080/project/{projectId}/sourcerepository/commit/{sha}?branch=master)
	// ==============================

	@GetMapping("/commit/{sha}")
	public String commitDetail(
			@PathVariable String projectId,
			@PathVariable String sha,
			@RequestParam(defaultValue = "master") String branch,
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
	// GitHub 동기화 (경로 : localhost:8080/project/{projectId}/sourcerepository/sync?branch=master)
	// ==============================

	@PostMapping("/sync")
	public String syncCommits(
			@PathVariable String projectId,
			@RequestParam(defaultValue = "master") String branch,
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
