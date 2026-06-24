package com.pixcel.app.roadmap.web;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.roadmap.service.RoadmapService;
import com.pixcel.app.roadmap.service.RoadmapVO;

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
    
    @GetMapping("/roadmap_create")
    public String createRoadmapForm(@AuthenticationPrincipal String userId, HttpSession session, Model model) {
    	
    	String projectId = getProjectIdFromSession(session);
    	
    model.addAttribute("projectId", projectId);
    return "roadmap/roadmap_create";
    }
    @PostMapping("/roadmap_create")
    public String createRoadmapSubmit(@ModelAttribute RoadmapVO roadmapVO, HttpSession session) {
    	String projectId = getProjectIdFromSession(session);
    	roadmapVO.setProjectId(projectId);
    	log.info("마일스톤 생성 요청 데이터: {}", roadmapVO);//로그 확인용
    	roadmapService.insertRoadmap(roadmapVO);
    	return "redirect:/roadmap/roadmap_list";
    }
    @GetMapping("/roadmap_list")
    public String getRoadmapList(@ModelAttribute RoadmapVO roadmapVO, Model model, HttpSession session) {
    	String projectId = getProjectIdFromSession(session);
    	roadmapVO.setProjectId(projectId);
    	List<RoadmapVO> roadmapList = roadmapService.getRoadmapList(roadmapVO);
    	model.addAttribute("roadmapList", roadmapList);
    	return "roadmap/roadmap_list";
    	
    }
    @GetMapping("/roadmap_update")
    public String updateRoadmapForm(@RequestParam("versionId") String versionId, HttpSession session, Model model) {
        String projectId = getProjectIdFromSession(session);
        
        // 2. 기존 로드맵 데이터 단건 조회 (본인 프로젝트의 데이터인지 확인 포함)
        RoadmapVO roadmap = roadmapService.getRoadmapDetail(versionId, projectId);
        
        // 3. 보안 검증: 데이터가 없거나 다른 프로젝트의 버전에 접근하려 할 때 차단
        if (roadmap == null) {
            log.warn("권한 없는 로드맵 수정 폼 접근 시도! versionId: {}", versionId);
            return "redirect:/roadmap/roadmap_list?error=unauthorized";
        }
        
        // 4. 뷰(HTML)로 기존 데이터 전달
        model.addAttribute("roadmap", roadmap);      
        
        return "roadmap/roadmap_update"; // (또는 edit.html 등 설정하신 파일명)
    }
    @PostMapping("/roadmap_update")
    public String updateRoadmapSubmit(@ModelAttribute RoadmapVO roadmapVO, HttpSession session) {  
        String projectId = getProjectIdFromSession(session);
        roadmapVO.setProjectId(projectId);
        
        if (roadmapVO.getDefaultYn() == null) {
            roadmapVO.setDefaultYn("N");
        }
        
        roadmapService.updateRoadmap(roadmapVO);
        return "redirect:/roadmap/roadmap_list";
    }
    @PostMapping("/roadmap_delete")
    public String deleteRoadmap(@RequestParam("versionId") String versionId, HttpSession session, RedirectAttributes rttr) {
        
        String projectId = getProjectIdFromSession(session);
        
        try {
            // 1. 삭제 시도
            roadmapService.deleteRoadmap(versionId, projectId);
            
            // 2. 성공 시 메시지
            rttr.addFlashAttribute("message", "로드맵이 정상적으로 삭제되었습니다.");
            return "redirect:/roadmap/roadmap_list";
            
        } catch (DataIntegrityViolationException e) {
            // 💡 핵심: DB에 엮인 하위 데이터가 있어서 삭제가 거부되었을 때
            log.warn("로드맵 삭제 실패 (하위 데이터 존재) - versionId: {}", versionId);
            rttr.addFlashAttribute("errorMessage", "해당 버전에 연결된 하위 항목(일감, 마일스톤, 문서 등)이 남아있어 삭제할 수 없습니다. 모두 지우거나 연결을 해제한 후 다시 시도해주세요.");
            
            // 로드맵은 상세 페이지가 없으므로, 목록 페이지로 돌려보냅니다.
            return "redirect:/roadmap/roadmap_list";
            
        } catch (Exception e) {
            // 기타 알 수 없는 에러
            log.error("로드맵 삭제 중 오류 발생", e);
            rttr.addFlashAttribute("errorMessage", "로드맵 삭제 중 알 수 없는 오류가 발생했습니다.");
            return "redirect:/roadmap/roadmap_list";
        }
    }
}
