package com.pixcel.app.repository.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.repository.service.RepositoryService;
import com.pixcel.app.repository.service.RepositoryVO;
//import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

//@AllProjectController
@Controller // 화면 경로를 반환함
@RequiredArgsConstructor // Service 객체 생성을 위한 생성자 주입 처리
@RequestMapping("/project/{projectId}/repository") // repository의 기본 URL 주소를 설정함.
public class RepositoryController {
	private final RepositoryService repositoryService;
	private final ProjectService projectService;

	// 1. 자료실 전체 조회 (요청주소 : localhost:8080/repository)
	@GetMapping
	public String getRepositoryList(@PathVariable("projectId") String projectId, RepositoryVO searchVO, Model model,
			RedirectAttributes rttr) {

		searchVO.setProjectId(projectId);

		model.addAttribute("sourceList", repositoryService.getSourceCodeList());

		// ✅ 추가: 날짜 검증 (startDate > endDate 불가)
		if (searchVO.getStartDate() != null && !searchVO.getStartDate().isEmpty() && searchVO.getEndDate() != null
				&& !searchVO.getEndDate().isEmpty()) {

			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date startDate = dateFormat.parse(searchVO.getStartDate());
				Date endDate = dateFormat.parse(searchVO.getEndDate());

				if (startDate.after(endDate)) {
					rttr.addFlashAttribute("errorMessage", "시작 날짜가 종료 날짜보다 늦을 수 없습니다.");
					return "redirect:/project/" + projectId + "/repository";
				}
			} catch (Exception e) {
				rttr.addFlashAttribute("errorMessage", "날짜 형식이 올바르지 않습니다.");
				return "redirect:/project/" + projectId + "/repository";
			}
		}

		// 1. 페이징 기본 설정
		int amount = 10;
		if (searchVO.getPage() <= 0)
			searchVO.setPage(1);
		searchVO.setStartRow((searchVO.getPage() - 1) * amount + 1);
		searchVO.setEndRow(searchVO.getPage() * amount);

		// 2. 데이터 조회
		List<RepositoryVO> list = repositoryService.getRepositoryList(searchVO);
		int total = repositoryService.getTotalCount(searchVO);

		// 3. 페이징 계산 로직 추가
		int totalPages = (int) Math.ceil((double) total / amount);
		int startPage = ((searchVO.getPage() - 1) / 5) * 5 + 1;
		int endPage = Math.min(startPage + 4, totalPages);

		// 4. 계산된 페이징 정보를 searchVO에 담거나 모델에 직접 추가
		searchVO.setTotalPage(totalPages);
		searchVO.setStartPage(startPage);
		searchVO.setEndPage(endPage);
		searchVO.setHasPrevious(searchVO.getPage() > 1);
		searchVO.setHasNext(searchVO.getPage() < totalPages);

		// 5. 모델에 데이터 및 페이징 정보 전달
		model.addAttribute("projectId", projectId);
		model.addAttribute("repositoryList", list);
		model.addAttribute("repositoryPage", searchVO);
		model.addAttribute("projectList", projectService.findAllProjects());

		return "repository/repositoryList";
	}

	// 2. 상세조회 (메서드명 중복 제거)
	@GetMapping("/detail")
	public String getRepositoryDetail(@PathVariable("projectId") String projectId, @RequestParam String fileId,
			@CookieValue(value = "userId", required = false) String userId, Model model) {
		RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
		model.addAttribute("repository", detail);
		model.addAttribute("userId", userId);
		model.addAttribute("projectId", projectId);
		return "repository/repositoryDetail";
	}

	// 3-1. 자료실 파일 등록 화면 이동페이지 (요청 주소 : localhost:8080/repository/new)
	@GetMapping("/new")
	public String createForm(@PathVariable("projectId") String projectId,
			@CookieValue(value = "userId", required = false) String userId, Model model) {

		// 로그인이 안 되어 있을 때(null 일 때) 로그인 페이지로 보냅니다.
		if (userId == null) {
			return "redirect:/login";
		}

		RepositoryVO repositoryVO = new RepositoryVO();
		repositoryVO.setProjectId(projectId); // 경로에서 받은 ID 설정
		repositoryVO.setUploadUserId(userId);

		model.addAttribute("repository", repositoryVO);
		return "repository/repositoryNew";
	}

	// 3-2. 자료실 파일 등록 처리 후 해당 유저의 목록으로 리다이렉트(요청 주소 : localhost:8080/repository/new)
	@PostMapping("/new")
	public String create(@PathVariable("projectId") String projectId, RepositoryVO repositoryVO,
			@RequestParam("uploadFile") MultipartFile uploadFile,
			@CookieValue(value = "userId", required = false) String userId) {

		repositoryVO.setProjectId(projectId); // 경로에서 받은 ID로 설정 보장
		repositoryVO.setUploadUserId(userId);

		// [중요] versionId가 누락되지 않도록 설정 (예: 1 또는 서비스에서 조회한 최신 버전 등)
		if (repositoryVO.getVersionId() == null) {
			repositoryVO.setVersionId("VERSION_ID_2606_0001"); // 실제 사용하는 버전 ID 값으로 교체하세요
		}

		repositoryService.registerRepository(repositoryVO, uploadFile);

		return "redirect:/project/" + projectId + "/repository/detail?fileId=" + repositoryVO.getFileId();
	}

	// 4-1. 자료실 파일 수정 화면 이동페이지 (요청 주소 : localhost:8080/repository/edit?fileId=파일ID)
	@GetMapping("/edit")
	public String editForm(@PathVariable("projectId") String projectId, // 경로에서 프로젝트 ID 수신
			@RequestParam String fileId, Model model) {

		RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
		model.addAttribute("repository", detail);
		model.addAttribute("projectId", projectId); // 화면에서 경로 생성 시 사용
		return "repository/repositoryEdit";
	}

	// 4-2. 자료실 파일 수정 처리 후 해당 유저의 목록으로 리다이렉트
	@PostMapping("/edit")
	public String edit(@PathVariable("projectId") String projectId, RepositoryVO repositoryVO,
			@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
			@CookieValue(value = "userId", required = false) String userId, RedirectAttributes rttr) {

		// 1. 로그인 확인
		if (userId == null) {
			return "redirect:/login";
		}

		// 2. 기본 정보 설정
		repositoryVO.setProjectId(projectId);
		repositoryVO.setUploadUserId(userId);

		// 3. 권한 체크 및 수정 처리
		try {
			repositoryService.modifyRepository(repositoryVO, uploadFile, userId);
		} catch (RuntimeException e) {
			// 권한 없음 또는 파일 없음 등의 오류 처리
			rttr.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/project/" + projectId + "/repository/detail?fileId=" + repositoryVO.getFileId();
		}

		return "redirect:/project/" + projectId + "/repository/detail?fileId=" + repositoryVO.getFileId();
	}

	// 삭제 처리: /project/{projectId}/repository/delete
	@PostMapping("/delete")
	public String delete(@PathVariable("projectId") String projectId, @RequestParam String fileId,
			@CookieValue(value = "userId", required = false) String userId) {
		if (userId == null)
			return "redirect:/login";
		repositoryService.removeRepository(fileId, userId);
		return "redirect:/project/" + projectId + "/repository";
	}

	@GetMapping("/download")
	public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@RequestParam String fileId)
			throws Exception {
		RepositoryVO fileInfo = repositoryService.getRepositoryDetail(fileId);
		if (fileInfo == null)
			return ResponseEntity.notFound().build();

		File file = new File(fileInfo.getFilePath());
		if (!file.exists()) {
			return ResponseEntity.notFound().build();
		}

		org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
		String encodedName = java.net.URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8").replace("+", "%20");

		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + encodedName + "\"")
				.header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/octet-stream").body(resource);
	}

	@GetMapping("/image")
	@ResponseBody
	public org.springframework.core.io.Resource getImage(@RequestParam String fileId) throws Exception {
		RepositoryVO fileInfo = repositoryService.getRepositoryDetail(fileId);

		// 파일 정보가 없거나 실제 물리 경로에 파일이 없는 경우 방어 코드
		if (fileInfo == null || fileInfo.getFilePath() == null)
			return null;

		File file = new File(fileInfo.getFilePath());
		if (!file.exists())
			return null;

		// 이미지를 브라우저가 직접 읽을 수 있게 리소스로 반환
		return new org.springframework.core.io.FileSystemResource(file);
	}

}