package com.pixcel.app.repository.web;

import java.io.File;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.repository.service.RepositoryService;
import com.pixcel.app.repository.service.RepositoryVO;

import lombok.RequiredArgsConstructor;

@Controller // 화면 경로를 반환함
@RequiredArgsConstructor // Service 객체 생성을 위한 생성자 주입 처리
@RequestMapping("/repository") // repository의 기본 URL 주소를 설정함.
public class RepositoryController {
	private final RepositoryService repositoryService;
	private final ProjectService projectService;

	// 1. 자료실 전체 조회 (요청주소 : localhost:8080/repository)
	@GetMapping
	public String getRepositoryList(RepositoryVO searchVO, Model model) {
		int amount = 10;
		if (searchVO.getPage() <= 0)
			searchVO.setPage(1);
		searchVO.setStartRow((searchVO.getPage() - 1) * amount + 1);
		searchVO.setEndRow(searchVO.getPage() * amount);

		// 1. 데이터 조회
		model.addAttribute("repositoryList", repositoryService.getRepositoryList(searchVO));

		// 2. 일감 목록 (Service를 통해 가져옴)
		model.addAttribute("projectList", projectService.findAllProjects());

		// 3. 페이징 계산 (DTO 없이 직접 계산)
		int total = repositoryService.getTotalCount(searchVO);
		int totalPages = (int) Math.ceil((double) total / amount);

		// 뷰에서 쓰기 편하게 model에 직접 담기
		model.addAttribute("currentPage", searchVO.getPage());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("hasPrevious", searchVO.getPage() > 1);
		model.addAttribute("hasNext", searchVO.getPage() < totalPages);

		return "repository/repositoryList";
	}

//	// 2. 자료실 등록한 사람의 자료 상세조회 (요청주소 : localhost:8080/repository/user?uploadUserId=유저ID)
//	@GetMapping("/user")
//	public String getRepositoryListByUserId(@RequestParam String fileId, Model model) {
//		RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
//		model.addAttribute("repository", detail);
//		return "repository/repositoryUserList";
//	}

	// 3. 자료실 상세조회 (요청주소 : localhost:8080/repository/detail?fileId=파일ID)
	@GetMapping("/detail")
	public String getRepositoryDetail(@RequestParam String fileId,
			@CookieValue(value = "userId", required = false) String userId, Model model) {
		RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
		model.addAttribute("repository", detail);
		model.addAttribute("userId", userId);
		return "repository/repositoryDetail";
	}

	// 4-1. 자료실 파일 등록 화면 이동페이지 (요청 주소 : localhost:8080/repository/new)
	@GetMapping("/new")
	public String createForm(@RequestParam(value = "projectId", required = false) String projectId,
			@RequestParam(value = "versionId", required = false) String versionId,
			@CookieValue(value = "userId", required = false) String userId, Model model) {

		// 로그인이 안 되어 있을 때(null 일 때) 로그인 페이지로 보냅니다.
		if (userId == null) {
			return "redirect:/login";
		}

		// 이전 화면에서 넘어온 프로젝트 ID와 버전 ID를 VO에 담아서 화면으로 전달함.
		RepositoryVO repositoryVO = new RepositoryVO();

		if (projectId != null)
			repositoryVO.setProjectId(projectId);
		if (versionId != null)
			repositoryVO.setVersionId(versionId);

		// 화면단이나 추후 처리를 위해 가져온 로그인 ID도 세팅해 줍니다.
		repositoryVO.setUploadUserId(userId);

		model.addAttribute("repository", repositoryVO);
		return "repository/repositoryNew";
	}

	// 4-2. 자료실 파일 등록 처리 후 해당 유저의 목록으로 리다이렉트(요청 주소 : localhost:8080/repository/new)
	@PostMapping("/new")
	public String create(RepositoryVO repositoryVO, @RequestParam("uploadFile") MultipartFile uploadFile,
			@CookieValue(value = "userId", required = false) String userId, RedirectAttributes rttr) {

		// 1. 세션에서 가져온 사용자 ID 설정
		repositoryVO.setUploadUserId(userId);

		// 2. 만약 repositoryVO.getProjectId()가 null이라면 에러 방지를 위해 기본값 혹은 로직 추가 가능
		// (hidden 필드에서 넘어온 값이 여기서 바인딩되어야 합니다)

		System.out.println("DEBUG - 최종 저장될 VO: " + repositoryVO.toString()); // 이 로그로 값 확인 가능

		repositoryService.registerRepository(repositoryVO, uploadFile);
		return "redirect:/repository/detail?fileId=" + repositoryVO.getFileId();
	}

	// 5-1. 자료실 파일 수정 화면 이동페이지 (요청 주소 : localhost:8080/repository/edit?fileId=파일ID)
	@GetMapping("/edit")
	public String editForm(@RequestParam String fileId, Model model) {
		RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
		model.addAttribute("repository", detail);
		return "repository/repositoryEdit";
	}

	// 5-2. 자료실 파일 수정 처리 후 해당 유저의 목록으로 리다이렉트
	@PostMapping("/edit")
	public String edit(RepositoryVO repositoryVO,
			@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
			@CookieValue(value = "userId", required = false) String userId, RedirectAttributes rttr) { // <--- 메시지를 화면으로
																										// 보낼 택배 상자
		// 1. 서비스에 데이터와 userId를 같이 넘깁니다.
		try {
			repositoryService.modifyRepository(repositoryVO, uploadFile, userId);

			// 성공했을 경우
			rttr.addFlashAttribute("msg", "success");
			rttr.addFlashAttribute("message", "파일이 수정되었습니다!");
		} catch (Exception e) {
			// 실패했을 경우 (권한 없음 등...)
			rttr.addFlashAttribute("msg", "error");
			rttr.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/repository/detail?fileId=" + repositoryVO.getFileId();
	}

	@PostMapping("/delete")
	public String delete(@RequestParam String fileId, @CookieValue(value = "userId", required = false) String userId) {
		if (userId == null)
			return "redirect:/login";
		repositoryService.removeRepository(fileId, userId);
		return "redirect:/repository";
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

}
