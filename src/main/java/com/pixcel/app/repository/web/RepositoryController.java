package com.pixcel.app.repository.web;

import java.io.File;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.repository.service.RepositoryService;
import com.pixcel.app.repository.service.RepositoryVO;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

@AllProjectController
@Controller // 화면 경로를 반환함
@RequiredArgsConstructor // Service 객체 생성을 위한 생성자 주입 처리
@RequestMapping("/repository") // repository의 기본 URL 주소를 설정함.
public class RepositoryController {
	private final RepositoryService repositoryService;
	private final ProjectService projectService;

	// 1. 자료실 전체 조회 (요청주소 : localhost:8080/repository)
	@GetMapping
	public String getRepositoryList(@PathVariable("projectId") String projectId, RepositoryVO searchVO, Model model) {
		
		searchVO.setProjectId(projectId);
		
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
		model.addAttribute("repositoryList", repositoryService.getRepositoryList(searchVO));
        model.addAttribute("projectList", projectService.findAllProjects());
        return "repository/repositoryList";
	}

	// 2. 상세조회 (메서드명 중복 제거)
    @GetMapping("/detail")
    public String getRepositoryDetail(@PathVariable("projectId") String projectId, 
                                      @RequestParam String fileId, 
                                      @CookieValue(value = "userId", required = false) String userId, 
                                      Model model) {
        RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
        model.addAttribute("repository", detail);
        model.addAttribute("userId", userId);
        model.addAttribute("projectId", projectId);
        return "repository/repositoryDetail";
    }

	// 3-1. 자료실 파일 등록 화면 이동페이지 (요청 주소 : localhost:8080/repository/new)
	@GetMapping("/new")
	public String createForm(@PathVariable("projectId") String projectId,
							 @CookieValue(value = "userId", required = false) String userId,
							 Model model) {

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
	public String create(@PathVariable("projectId") String projectId,
            RepositoryVO repositoryVO, 
            @RequestParam("uploadFile") MultipartFile uploadFile,
            @CookieValue(value = "userId", required = false) String userId) {

		repositoryVO.setProjectId(projectId); // 경로에서 받은 ID로 설정 보장
        repositoryVO.setUploadUserId(userId);
        
        repositoryService.registerRepository(repositoryVO, uploadFile);
        
        return "redirect:/project/" + projectId + "/repository/detail?fileId=" + repositoryVO.getFileId();
	}

	// 4-1. 자료실 파일 수정 화면 이동페이지 (요청 주소 : localhost:8080/repository/edit?fileId=파일ID)
	@GetMapping("/edit")
	public String editForm(
	        @PathVariable("projectId") String projectId, // 경로에서 프로젝트 ID 수신
	        @RequestParam String fileId, 
	        Model model) {
	    
	    RepositoryVO detail = repositoryService.getRepositoryDetail(fileId);
	    model.addAttribute("repository", detail);
	    model.addAttribute("projectId", projectId); // 화면에서 경로 생성 시 사용
	    return "repository/repositoryEdit";
	}

	// 4-2. 자료실 파일 수정 처리 후 해당 유저의 목록으로 리다이렉트
	@PostMapping("/edit")
    public String edit(@PathVariable("projectId") String projectId,
                       RepositoryVO repositoryVO,
                       @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
                       @CookieValue(value = "userId", required = false) String userId, 
                       RedirectAttributes rttr) {
        repositoryVO.setProjectId(projectId);
        repositoryService.modifyRepository(repositoryVO, uploadFile, userId);
        return "redirect:/project/" + projectId + "/repository/detail?fileId=" + repositoryVO.getFileId();
    }

	// 삭제 처리: /project/{projectId}/repository/delete
	@PostMapping("/delete")
    public String delete(@PathVariable("projectId") String projectId, 
                         @RequestParam String fileId, 
                         @CookieValue(value = "userId", required = false) String userId) {
        if (userId == null) return "redirect:/login";
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

}
