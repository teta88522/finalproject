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
		int amount = 5;
	    if (searchVO.getPage() <= 0) searchVO.setPage(1);
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
			@CookieValue(value = "userId", required = false) String userId) {

		if (userId != null) {
			repositoryVO.setUploadUserId(userId);
		} else {
			return "redirect:/login";
		}

		// 주소창에 파라미터가 없어서 누락된 경우, 테스트용 디폴트 ID를 강제로 꽂아줍니다
		if (repositoryVO.getProjectId() == null || repositoryVO.getProjectId().isEmpty()) {
			repositoryVO.setProjectId("PROJECT_ID_2606_0007");
		}
		if (repositoryVO.getVersionId() == null || repositoryVO.getVersionId().isEmpty()) {
			repositoryVO.setVersionId("VERSION_ID_2606_0001");
		}

		// 2. 파일 코드 누락시 디폴트
		if (repositoryVO.getFileCode() == null || repositoryVO.getFileCode().isEmpty()) {
			repositoryVO.setFileCode("FILE_" + System.currentTimeMillis());
		}
		// 3. 파일 버전
		if (repositoryVO.getFileVersion() == null || repositoryVO.getFileVersion().isEmpty()) {
			repositoryVO.setFileVersion("1");
		}
		// 4. 파일 사용 여부
		if (repositoryVO.getFileUseYn() == null || repositoryVO.getFileUseYn().isEmpty()) {
			repositoryVO.setFileUseYn("g001");
		}
		// 5. 외부 연결 주소
		if (repositoryVO.getConnectAddress() == null) {
			repositoryVO.setConnectAddress("-");
		}

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
			@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
		repositoryService.modifyRepository(repositoryVO, uploadFile);
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
	public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@RequestParam String fileId) throws Exception {
	    RepositoryVO fileInfo = repositoryService.getRepositoryDetail(fileId);
	    
	    String fullPath = fileInfo.getFilePath();
	    
	    File file = new File(fullPath);

	    if (!file.exists()) {
	        return ResponseEntity.notFound().build();
	    }

	    org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
	    String encodedName = java.net.URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8").replace("+", "%20");
	    
	    return ResponseEntity.ok()
	            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
	            .body(resource);
	}
	
}
