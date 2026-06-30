package com.pixcel.app.notice.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pixcel.app.notice.service.NoticeRequestDTO;
import com.pixcel.app.notice.service.NoticeService;
import com.pixcel.app.user.security.CustomUserDetails;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/notice") 
@RequiredArgsConstructor
@AllProjectController
public class NoticeController {
	private final NoticeService noticeService;
	
	@GetMapping("/BoardCreate")
	public String createBoardForm(
				@AuthenticationPrincipal CustomUserDetails userDetails
				,@PathVariable("projectId") String projectId,
				Model model) {
		
		model.addAttribute("projectId", projectId);
		model.addAttribute("noticeRequestDTO", new NoticeRequestDTO());
		
		return "notice/BoardCreate";
	}
	
	@PostMapping("/BoardCreate")
	public String createBoardForm(
			@ModelAttribute NoticeRequestDTO noticeRequestDto,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable("projectId") String projectId
			) {
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
		
		return "notice/BoardList";
	}
}
