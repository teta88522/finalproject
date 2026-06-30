package com.pixcel.app.web;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.pixcel.app.project.service.ProjectModulesVO;
import com.pixcel.app.project.service.ProjectService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {
	
	@Autowired
	private ProjectService projectService;
	
    // admin 사이드바 URL 목록
    private static final List<String> ADMIN_URIS = List.of(
        "/admin", "/codevalue", "/issuetype", "/issuestatus","/workflow", "/roles"
    );

    // project 사이드바 URL 목록
    private static final List<String> PROJECT_URIS = List.of(
        "/document", "/issues", "/kanban", "/members", 
        "/milestones", "/roadmap", "/repository", "/projectdetail", "/project","/tests", "/sourcerepository"
    );
    
 // project 사이드바 URL 목록
    private static final List<String> TEAM_URIS = List.of(
        "/team"
    );


    @ModelAttribute
    public void addMenuType(HttpServletRequest request, Model model) {
        String uri = request.getRequestURI();

        boolean isAdmin = ADMIN_URIS.stream()
                .anyMatch(uri::startsWith);

        boolean isProject = PROJECT_URIS.stream()
                .anyMatch(uri::startsWith);
        
        boolean isTeam = TEAM_URIS.stream()
                .anyMatch(uri::startsWith);
        
        
        if (isAdmin) {
            model.addAttribute("menuType", "admin");
        } else if (isProject) {
            model.addAttribute("menuType", "project");
            String[] paths = uri.split("/");

            if (paths.length >= 3) {
                String projectId = paths[2];
                
                List<String> moduleCodes = projectService.selectAllModuleProjects(projectId)
                        .stream()
                        .map(ProjectModulesVO::getModuleCode)
                        .toList();
                model.addAttribute("moduleCodes", moduleCodes);
            }
        } else if (isTeam) {
        	model.addAttribute("menuType", "team");
        }
        // 둘 다 아니면 menuType 안 넣음 (사이드바 없음)
        
     // 쿠키값 처리
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("subscribeYn".equals(cookie.getName())) {
                    model.addAttribute("subscribeYn", cookie.getValue());
                }
                if ("authYn".equals(cookie.getName())) {
                    model.addAttribute("authYn", cookie.getValue());
                }
                if ("userId".equals(cookie.getName())) {
                    model.addAttribute("userId", cookie.getValue());
                }
                if ("userName".equals(cookie.getName())) {
                    model.addAttribute("userName", URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8));
                }
            }
        }
    }

    @ExceptionHandler(LoginRequiredException.class)
    public String handleLoginRequiredException() {
        return "redirect:/login";
    }
}
