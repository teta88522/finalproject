package com.pixcel.app.bug.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.pixcel.app.bug.service.BugSearchVO;
import com.pixcel.app.bug.service.BugService;
import com.pixcel.app.bug.service.BugVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BugController {
	private final BugService bugService;
	
	//목록
	@GetMapping("/project/{projectId}/bugs")
	public String bugList(@PathVariable String projectId,
						  @ModelAttribute BugSearchVO bugSearchVO,
						  Model model) {
		
		bugSearchVO.setProjectId(projectId);
		
		List<BugVO> bugList = bugService.selectBugList(bugSearchVO);
		
		model.addAttribute("bugList", bugList);
		model.addAttribute("searchVO", bugSearchVO);
		model.addAttribute("projectId",projectId);
		
		return "bug/bugList";
	}
}
