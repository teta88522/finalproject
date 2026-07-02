package com.pixcel.app.roadmap.web;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.roadmap.service.RoadmapService;
import com.pixcel.app.roadmap.service.RoadmapVO;
import com.pixcel.app.user.security.CustomUserDetails;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/roadmap") 
@RequiredArgsConstructor
@AllProjectController
public class RoadmapController {
    
    private final RoadmapService roadmapService;
    
    @GetMapping("/setting_create")
    public String createRoadmapForm(@AuthenticationPrincipal CustomUserDetails userDetails, 
                                    @PathVariable("projectId") String projectId,
                                    Model model) {
        model.addAttribute("projectId", projectId);
        
        return "roadmap/setting_create"; 
    }

    @PostMapping("/setting_create")
    public String createRoadmapSubmit(@ModelAttribute RoadmapVO roadmapVO, 
                                      @PathVariable("projectId") String projectId) {
        roadmapVO.setProjectId(projectId);
        
        roadmapService.insertRoadmap(roadmapVO);
        // 💡 핵심 2: 404 에러 방지를 위해 리다이렉트는 무조건 절대 경로(풀 주소)로 적습니다.
        return "redirect:/project/" + projectId + "/roadmap/roadmap_list"; 
    }

    @GetMapping("/setting_list")
    public String getRoadmapList(@ModelAttribute RoadmapVO roadmapVO,
                                 @PathVariable("projectId") String projectId,
                                 Model model) {
        roadmapVO.setProjectId(projectId);
        List<RoadmapVO> roadmapList = roadmapService.getSettingList(roadmapVO);
        model.addAttribute("roadmapList", roadmapList);
        
        return "roadmap/setting_list"; 
    }

    @GetMapping("/setting_update")
    public String updateRoadmapForm(@RequestParam("versionId") String versionId, 
                                    @PathVariable("projectId") String projectId,
                                    Model model) {
        
        RoadmapVO roadmap = roadmapService.getsettingDetail(versionId, projectId);
        
        if (roadmap == null) {
            log.warn("권한 없는 접근!");
            return "redirect:/project/" + projectId + "/roadmap/setting_list?error=unauthorized";
        }
        
        model.addAttribute("roadmap", roadmap);      
        return "roadmap/setting_update"; 
    }

    @PostMapping("/setting_update")
    public String updateRoadmapSubmit(@ModelAttribute RoadmapVO roadmapVO,
                                      @RequestParam("versionId") String versionId, 
                                      @PathVariable("projectId") String projectId, 
                                      Model model) {  
        
        roadmapVO.setProjectId(projectId);
        
        if (roadmapVO.getDefaultYn() == null) {
            roadmapVO.setDefaultYn("N");
        }
        
        roadmapService.updateRoadmap(roadmapVO);
        
        return "redirect:/project/" + projectId + "/roadmap/setting_list";
    }

    @PostMapping("/setting_delete")
    public String deleteRoadmap(@RequestParam("versionId") String versionId, 
                                @PathVariable("projectId") String projectId,
                                RedirectAttributes rttr) {
        try {
            roadmapService.deleteRoadmap(versionId, projectId);
            rttr.addFlashAttribute("message", "로드맵이 정상적으로 삭제되었습니다.");
            
        } catch (DataIntegrityViolationException e) {
            rttr.addFlashAttribute("errorMessage", "해당 버전에 연결된 하위 항목이 남아있어 삭제할 수 없습니다.");
            
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "로드맵 삭제 중 알 수 없는 오류가 발생했습니다.");
        }
        
        return "redirect:/project/" + projectId + "/roadmap/setting_list";
    }
    
    @GetMapping("/roadmap_list")
    public String getRoadmapListFull(@PathVariable("projectId") String projectId, Model model) {
        
        List<RoadmapVO> roadmapList = roadmapService.getRoadmapFull(projectId);
        
        model.addAttribute("roadmapList", roadmapList);
        model.addAttribute("projectId", projectId);
        
        return "roadmap/roadmap_list";
    }
    
    @GetMapping("/roadmap_detail")
    public String getRoadmapDetail(@RequestParam("versionId") String versionId,
                                   @PathVariable("projectId") String projectId, 
                                   Model model) {
        
        RoadmapVO roadmap = roadmapService.getRoadmapDetail(projectId, versionId);
        List<MilestonesVO> milestones = roadmapService.getRoadmapMilestones(projectId, versionId);
        
        roadmap.setMilestoneList(milestones);
        model.addAttribute("roadmap", roadmap);
        
        return "roadmap/roadmap_detail";
    }
    
    @PostMapping("/roadmap_detail")
    public String completeRoadmap(@RequestParam("versionId") String versionId, 
                                  @PathVariable("projectId") String projectId,
                                  RedirectAttributes rttr) {
        try {
            roadmapService.updateCompletion(versionId, projectId);
            rttr.addFlashAttribute("message", "로드맵이 완료 처리되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "완료 처리 중 오류가 발생했습니다.");
        }
        
        return "redirect:/project/" + projectId + "/roadmap/roadmap_detail";
    }
}