package com.pixcel.app.roadmap.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pixcel.app.roadmap.service.RoadmapService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapController {
	private final RoadmapService roadmapService;
	
    //session 처리
    private String getProjectIdFromSession(HttpSession session) {
        String projectId = (String) session.getAttribute("currentProjectId");
        
        // [임시 대응] 세션에 값이 없으면 더미 ID 강제 설정
        if (projectId == null) {
            projectId = "PROJECT_ID_2606_0001";
            session.setAttribute("currentProjectId", projectId);
            log.info("세션에 프로젝트 ID가 없어 더미 ID를 세팅합니다: {}", projectId);
        }
        return projectId;
    }
    
    @GetMapping("/create")
    public String createRoadmapForm(@AuthenticationPrincipal String userId, HttpSession session, Model model) {
    	
    	String projectId = getProjectIdFromSession(session);
    	
    model.addAttribute("projectId", projectId);
    return "roadmap/create";
    }
}
