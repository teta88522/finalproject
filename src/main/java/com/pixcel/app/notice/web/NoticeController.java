package com.pixcel.app.notice.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.notice.service.NoticeRequestDTO;
import com.pixcel.app.notice.service.NoticeService;
import com.pixcel.app.notice.service.PostRequestDTO;
import com.pixcel.app.notice.service.PostSearchDTO;
import com.pixcel.app.notice.service.PostService;
import com.pixcel.app.user.security.CustomUserDetails;
import com.pixcel.app.web.AllProjectController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/notice") 
@RequiredArgsConstructor
@AllProjectController
public class NoticeController {
	private final NoticeService noticeService;
	private final PostService postService;
	private final FileService fileService;
	// -- 권한 처리
	private final com.pixcel.app.issues.mapper.IssuesMapper issuesMapper;
	
	private boolean checkProjectPermission(String projectId, String userId, String permissionCode) {
		if (userId == null || projectId == null) {
			return false;
		}
		return issuesMapper.countProjectPermission(projectId, userId, permissionCode, null) > 0;
	}
	// -- 권한 처리 끝
	
	@GetMapping("/BoardCreate")
	public String createBoardForm(
				@AuthenticationPrincipal CustomUserDetails userDetails
				,@PathVariable("projectId") String projectId,
				Model model,
				RedirectAttributes rttr) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p009")) {
			rttr.addFlashAttribute("errorMessage", "게시판 관리 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardList";
		}
		// -- 권한 처리 끝
		
		model.addAttribute("projectId", projectId);
		model.addAttribute("noticeRequestDTO", new NoticeRequestDTO());
		
		return "notice/BoardCreate";
	}
	
	@PostMapping("/BoardCreate")
	public String createBoardForm(
			@ModelAttribute NoticeRequestDTO noticeRequestDto,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			RedirectAttributes rttr
			) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p009")) {
			rttr.addFlashAttribute("errorMessage", "게시판 관리 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardList";
		}
		// -- 권한 처리 끝
		noticeRequestDto.setCreatedBy(userDetails.getUsername());
		noticeService.createNoticeBoard(noticeRequestDto);
		return "redirect:/project/" + projectId + "/notice/BoardList";
	}
	
	@GetMapping("/BoardList")
	public String BoardList(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			Model model
			) {
		List<NoticeRequestDTO> boardList = noticeService.getBoardList(projectId);
		model.addAttribute("boardList", boardList);
		
		// -- 권한 처리
		boolean canManageBoard = checkProjectPermission(projectId, userDetails.getUsername(), "p009");
		model.addAttribute("canManageBoard", canManageBoard);
		// -- 권한 처리 끝
		
		// 1. 이미 구해온 게시판 리스트를 Map<boardId, boardName> 형태로 자바 메모리 고속 변환
		Map<String, String> boardNameMap = boardList.stream()
				.collect(Collectors.toMap(NoticeRequestDTO::getBoardId, NoticeRequestDTO::getBoardName, (v1, v2) -> v1));
		
	    List<PostRequestDTO> latestPosts = postService.getLatestPosts(projectId);
		List<PostRequestDTO> popularPosts = postService.getPopularPosts(projectId);
		
		// 2. DB 조인 없이 자바 메모리 상에서 매핑하여 뱃지명 주입!
		if (latestPosts != null) {
			latestPosts.forEach(post -> post.setBoardName(boardNameMap.getOrDefault(post.getBoardId(), "알 수 없음")));
		}
		if (popularPosts != null) {
			popularPosts.forEach(post -> post.setBoardName(boardNameMap.getOrDefault(post.getBoardId(), "알 수 없음")));
		}
		
		model.addAttribute("latestPosts", latestPosts);
		model.addAttribute("popularPosts", popularPosts);
		model.addAttribute("projectId", projectId);

		return "notice/BoardList";
	}
	
	@GetMapping("/BoardDetail")
	public String BoardDetail(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			@ModelAttribute PostSearchDTO searchDTO,
			@PageableDefault(size = 10 , sort= "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
			Model model
			) {
		NoticeRequestDTO boardDetail = noticeService.getBoardDetail(boardId);
		Page<PostRequestDTO> postPage = postService.getPostListByBoardId(boardId, searchDTO, pageable);
		
		// -- 권한 처리
		String userId = userDetails.getUsername();
		model.addAttribute("canManageBoard", checkProjectPermission(projectId, userId, "p009"));
		model.addAttribute("canAddPost",     checkProjectPermission(projectId, userId, "p010"));
		model.addAttribute("canEditPost",    checkProjectPermission(projectId, userId, "p011"));
		model.addAttribute("canDeletePost",  checkProjectPermission(projectId, userId, "p012"));
		model.addAttribute("canEditOwnPost",   checkProjectPermission(projectId, userId, "p013"));
		model.addAttribute("canDeleteOwnPost", checkProjectPermission(projectId, userId, "p014"));
		// -- 권한 처리 끝
		
		model.addAttribute("board",boardDetail);
		model.addAttribute("postPage", postPage);
		model.addAttribute("searchDTO", searchDTO);
		model.addAttribute("projectId", projectId);
		
		return "notice/BoardDetail";
	}
	
	@GetMapping("/PostCreate")
	public String PostCreate(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			RedirectAttributes rttr,
			Model model
			) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p010")) {
			rttr.addFlashAttribute("errorMessage", "게시글 추가 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardDetail?boardId=" + boardId;
		}
		// -- 권한 처리 끝
		model.addAttribute("boardId", boardId);
		model.addAttribute("projectId", projectId);
		model.addAttribute("postRequestDTO", new PostRequestDTO());
		return "notice/PostCreate";
	}
	
	@PostMapping("/PostCreate")
	public String PostCreate(@ModelAttribute PostRequestDTO postDTO,
	                       @RequestParam("boardId") String boardId,
	                       @PathVariable("projectId") String projectId,
						   @RequestParam(value = "files", required = false) List<MultipartFile> files,
	                       @AuthenticationPrincipal CustomUserDetails userDetails,
	                       RedirectAttributes rttr) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p010")) {
			rttr.addFlashAttribute("errorMessage", "게시글 추가 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardDetail?boardId=" + boardId;
		}
		// -- 권한 처리 끝
		postDTO.setCreatedBy(userDetails.getUsername());
		
	    postService.createPost(postDTO);
	    
		if (files != null && !files.isEmpty()) {
            List<MultipartFile> uploadFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
            if (!uploadFiles.isEmpty()) {
                FileDTO uploadDTO = new FileDTO();
                uploadDTO.setProjectId(projectId);
                uploadDTO.setFileCode("f007");
                uploadDTO.setUploadUserId(userDetails.getUsername());
                uploadDTO.setConnectAddress(postDTO.getPostId()); // 연동된 postId 지정
                
                fileService.uploadFile(uploadFiles, uploadDTO);
            }
        }


	    return "redirect:/project/" + projectId + "/notice/BoardDetail?boardId=" + boardId;
	}
	
	@GetMapping("/post_detail")
	public String PostDetail(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			@RequestParam("postId") String postId,
			Model model
			) {
		PostRequestDTO postDetail = postService.getPostDetail(postId);
		
		// 게시판 상세 정보를 가져와 게시판 이름 모델에 바인딩
		NoticeRequestDTO boardDetail = noticeService.getBoardDetail(boardId);
		model.addAttribute("boardName", boardDetail.getBoardName());
		
		// 첨부파일 리스트 조회 후 모델 바인딩 누락 복구
		List<FileVO> files = fileService.selectAll(postId, null);
		model.addAttribute("files", files);
		
		// -- 권한 처리
		String userId = userDetails.getUsername();
		boolean isAuthor = userId.equals(postDetail.getCreatedBy());
		boolean canEdit   = checkProjectPermission(projectId, userId, "p011")
						|| (isAuthor && checkProjectPermission(projectId, userId, "p013"));
		boolean canDelete = checkProjectPermission(projectId, userId, "p012")
						|| (isAuthor && checkProjectPermission(projectId, userId, "p014"));
		model.addAttribute("canEdit",   canEdit);
		model.addAttribute("canDelete", canDelete);
		// -- 권한 처리 끝
  
		model.addAttribute("post", postDetail);
		model.addAttribute("projectId", projectId);
		model.addAttribute("boardId", boardId);
		
		return "notice/post_detail";
	}
	
	// 1. 수정 화면 이동 (기존 정보 및 업로드된 파일 목록 바인딩)
	@GetMapping("/files/{fileId}/download")
	public void downloadPostFile(@PathVariable("fileId") String fileId,
	                             @AuthenticationPrincipal CustomUserDetails userDetails,
	                             HttpServletResponse response) throws IOException {
		fileService.downloadOne(fileId, response, userDetails.getUsername());
	}
	
	// 1. 수정 화면 이동 (기존 정보 및 업로드된 파일 목록 바인딩)
	// 1. 수정 화면 이동 (기존 정보 및 업로드된 파일 목록 바인딩)
	@GetMapping("/PostUpdate")
	public String PostEdit(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			@RequestParam("postId") String postId,
			RedirectAttributes rttr,
			Model model
			) {
		PostRequestDTO postDetail = postService.getPostDetail(postId);
		
		// -- 권한 처리
		String userId = userDetails.getUsername();
		boolean isAuthor = userId.equals(postDetail.getCreatedBy());
		boolean canEdit = checkProjectPermission(projectId, userId, "p011")
					|| (isAuthor && checkProjectPermission(projectId, userId, "p013"));
		if (!canEdit) {
			rttr.addFlashAttribute("errorMessage", "게시글 편집 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/post_detail?boardId=" + boardId + "&postId=" + postId;
		}
		// -- 권한 처리 끝
		
		List<FileVO> files = fileService.selectAll(postId, null);
		
		model.addAttribute("post", postDetail);
		model.addAttribute("files", files);
		model.addAttribute("projectId", projectId);
		model.addAttribute("boardId", boardId);
		
		return "notice/PostUpdate";
	}
	
	// 2. 수정 처리 완료 실행 핸들러
	@PostMapping("/PostUpdate")
	public String PostUpdate(
			@ModelAttribute PostRequestDTO postDTO,
			@RequestParam("boardId") String boardId,
			@PathVariable("projectId") String projectId,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "deleteFileIds", required = false) List<String> deleteFileIds,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			RedirectAttributes rttr
			) {
		// -- 권한 처리
		String userId = userDetails.getUsername();
		PostRequestDTO existing = postService.getPostDetail(postDTO.getPostId());
		boolean isAuthor = userId.equals(existing.getCreatedBy());
		boolean canEdit = checkProjectPermission(projectId, userId, "p011")
					|| (isAuthor && checkProjectPermission(projectId, userId, "p013"));
		if (!canEdit) {
			rttr.addFlashAttribute("errorMessage", "게시글 편집 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/post_detail?boardId=" + boardId + "&postId=" + postDTO.getPostId();
		}
		// -- 권한 처리 끝
		// 혹시라도 브라우저 캐시나 중복 전송으로 콤마(,)가 섞여 들어오는 현상을 차단하는 방어 코드
		if (boardId != null && boardId.contains(",")) {
			boardId = boardId.split(",")[0].trim();
		}
		if (postDTO.getBoardId() != null && postDTO.getBoardId().contains(",")) {
			postDTO.setBoardId(postDTO.getBoardId().split(",")[0].trim());
		}
		
		// 현재 로그인한 사용자를 수정자로 세팅
		postDTO.setCreatedBy(userDetails.getUsername()); 
		
		// 본문 수정 및 체크한 파일 삭제 서비스 호출
		postService.updatePost(postDTO, deleteFileIds);
		
		// 신규로 첨부한 파일이 존재하면 업로드 수행
		if (files != null && !files.isEmpty()) {
			List<MultipartFile> uploadFiles = files.stream()
				.filter(file -> file != null && !file.isEmpty())
				.toList();
			if (!uploadFiles.isEmpty()) {
				FileDTO uploadDTO = new FileDTO();
				uploadDTO.setProjectId(projectId);
				uploadDTO.setFileCode("f008");
				uploadDTO.setUploadUserId(userDetails.getUsername());
				uploadDTO.setConnectAddress(postDTO.getPostId());
				
				fileService.uploadFile(uploadFiles, uploadDTO);
			}
		}
		
		// boardId 변수 대신 postDTO.getBoardId()를 사용하여 안전하게 리다이렉트 조립
		return "redirect:/project/" + projectId + "/notice/post_detail?boardId=" + postDTO.getBoardId() + "&postId=" + postDTO.getPostId();
	}
	
	// 3. 게시글 삭제 처리 핸들러
	@GetMapping("/PostDelete")
	public String PostDelete(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			@RequestParam("postId") String postId,
			RedirectAttributes rttr
			) {
		// 혹시라도 쉼표(,)나 큰따옴표(")가 섞여서 들어오는 현상을 방지하는 방어 코드
		if (boardId != null) {
			boardId = boardId.replace("\"", "").split(",")[0].trim();
		}
		if (postId != null) {
			postId = postId.replace("\"", "").split(",")[0].trim();
		}
		
		// -- 권한 처리
		String userId = userDetails.getUsername();
		PostRequestDTO target = postService.getPostDetail(postId);
		boolean isAuthor = userId.equals(target.getCreatedBy());
		boolean canDelete = checkProjectPermission(projectId, userId, "p012")
					|| (isAuthor && checkProjectPermission(projectId, userId, "p014"));
		if (!canDelete) {
			rttr.addFlashAttribute("errorMessage", "게시글 삭제 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/post_detail?boardId=" + boardId + "&postId=" + postId;
		}
		// -- 권한 처리 끝
		
		postService.deletePost(postId);
		return "redirect:/project/" + projectId + "/notice/BoardDetail?boardId=" + boardId;
	}

	@GetMapping("/BoardUpdate")
	public String BoardEdit(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			RedirectAttributes rttr,
			Model model
			) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p009")) {
			rttr.addFlashAttribute("errorMessage", "게시판 관리 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardList";
		}
		// -- 권한 처리 끝
		// 기존 게시판 정보 상세 조회 후 모델 바인딩
		NoticeRequestDTO boardDetail = noticeService.getBoardDetail(boardId);
		model.addAttribute("board", boardDetail);
		model.addAttribute("projectId", projectId);
		return "notice/BoardUpdate";
	}

	@PostMapping("/BoardUpdate")
	public String BoardUpdate(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@ModelAttribute NoticeRequestDTO noticeRequestDto,
			RedirectAttributes rttr
			) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p009")) {
			rttr.addFlashAttribute("errorMessage", "게시판 관리 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardList";
		}
		// -- 권한 처리 끝
		noticeService.updateBoard(noticeRequestDto);
		return "redirect:/project/" + projectId + "/notice/BoardList";
	}

	@GetMapping("/BoardDelete")
	public String BoardDelete(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId,
			@RequestParam("boardId") String boardId,
			RedirectAttributes redirectAttributes
			) {
		// -- 권한 처리
		if (!checkProjectPermission(projectId, userDetails.getUsername(), "p009")) {
			redirectAttributes.addFlashAttribute("errorMessage", "게시판 관리 권한이 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardList";
		}
		// -- 권한 처리 끝
		boolean isDeleted = noticeService.deleteBoard(boardId);
		
		if (!isDeleted) {
			redirectAttributes.addFlashAttribute("errorMessage", "게시글이 존재하는 게시판은 삭제할 수 없습니다.");
			return "redirect:/project/" + projectId + "/notice/BoardUpdate?boardId=" + boardId;
		}
		
		return "redirect:/project/" + projectId + "/notice/BoardList";
	}
}
