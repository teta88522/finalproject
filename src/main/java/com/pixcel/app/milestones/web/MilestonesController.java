package com.pixcel.app.milestones.web;

import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.web.AllProjectController;

import jakarta.servlet.http.HttpSession;

import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j //로그를 남겨주는 어노테이션
@Controller
@RequestMapping("/milestones")
@RequiredArgsConstructor
@AllProjectController
public class MilestonesController {

    private final MilestonesService milestonesService; //서비스를 받아 mybatis와 연결
    
    //마일스톤 생성화면
    @GetMapping("/create")
    public String createMilestoneForm(@AuthenticationPrincipal String userId,
    								  @PathVariable("projectId") String projectId
    									,Model model) {
  
   
    
    List<MilestonesMemberDTO> managerList = milestonesService.getManagerList(projectId); //멤버 담당자 조회 테스트 향후 수정필요
    model.addAttribute("managerList", managerList); //화면에 팀원 목록을 넘겨줌
    model.addAttribute("projectId", projectId);
    return "milestones/create";
    }
    
    //생성화면 내 일감 검색
    //첫 번째 일감을 선택한 후에는 versionId가 넘어와서 해당 버전의 일감만 찾게 됩니다.
    @ResponseBody //데이터만 리턴할 때 사용 자바 객체(List)를 화면이 이해할 수 있는 JSON 문자열로 바꿔서 브라우저로 전송
    @GetMapping("/api/issues/search") //api사용 이유: JSON 같은 순수 데이터만 가져오려는 거구나
    public List<IssuesVO>searchIssue(
    		@RequestParam("keyword")String keyword,
    		@RequestParam(value="versionId",required = false) String versionId,
    		@PathVariable("projectId") String projectId
    		){
    
     
    return milestonesService.getIssueList(keyword, versionId, projectId); //1. 서비스에 검색어와 버전ID를 넘겨서 조건에 맞는 일감 목록을 가져옵니다.
    }
    
    //마일스톤 데이터 실제 저장
    //화면에서 저장시 form데이터가 RequestDTO에 담겨서 컨트롤러 옴
    @PostMapping("/create")
    public String createMilestoneSubmit(
    		@ModelAttribute MilestonesVO milestoneVO,
    		@PathVariable("projectId") String projectId) {
    	
    	milestoneVO.setProjectId(projectId);
    	milestonesService.createMilestone(milestoneVO);
    	return "redirect:/project/" + projectId + "/milestones/detail?id=" + milestoneVO.getMilestoneId();
    	
    }
    
    @GetMapping("/detail")
    public String milestoneDetail(@RequestParam("id") String milestoneId,
    							  @PathVariable("projectId") String projectId
    			, Model model) {
    	
        // SQL JOIN 문을 통해 담당자 이름(managerName)까지 한 번에 바인딩되어 넘어옵니다.
    	MilestonesVO detailVO = milestonesService.getMilestoneDetail(milestoneId, projectId);
    	List<IssuesVO> connectedIssues = milestonesService.selectConnectedIssues(milestoneId);
        if (detailVO == null) {
            return "redirect:/project/" + projectId + "/milestones/list"; // 조회 실패 시 메인으로 리다이렉트
        }
        
        model.addAttribute("milestone", detailVO);
        model.addAttribute("issues", connectedIssues);
        model.addAttribute("projectId", projectId);
        return "milestones/detail"; 
    }
    
 // 마일스톤 수정 화면 진입
    @GetMapping("/update")
    public String updateMilestoneForm(@RequestParam("id") String milestoneId,
    		 						@PathVariable("projectId") String projectId, 
    		 						Model model) {
    	
  
    	MilestonesVO milestone = milestonesService.getMilestoneDetail(milestoneId,projectId);
        
    	if (milestone == null) {
            log.warn("권한 없는 수정 폼 접근 시도! milestoneId: {}", milestoneId);
            return "redirect:/project/" + projectId + "/milestones/list?error=unauthorized";
        }
    	
        List<MilestonesMemberDTO> managerList = milestonesService.getManagerList(projectId);
        List<IssuesVO> connectedIssues = milestonesService.selectConnectedIssues(milestoneId);
        // 3. 뷰(HTML)로 데이터 전달
        model.addAttribute("milestone", milestone);      // 수정 폼에 채워질 기존 데이터
        model.addAttribute("managerList", managerList);  // 담당자 선택 콤보박스 목록
        model.addAttribute("connectedIssues", connectedIssues);
        model.addAttribute("projectId", projectId);
        return "milestones/update"; // update.html 렌더링
    }
    
 // 마일스톤 데이터 실제 수정 (저장)
    @PostMapping("/update")
    public String updateMilestoneSubmit(
        @ModelAttribute MilestonesVO requestDTO, @PathVariable("projectId") String projectId) {  
    	
    	requestDTO.setProjectId(projectId);
        milestonesService.updateMilestone(requestDTO);
        return "redirect:/project/" + projectId + "/milestones/detail?id=" + requestDTO.getMilestoneId();
    }
    //삭제
    @PostMapping("/delete")
    public String deleteMilestone( @RequestParam("milestoneId") String milestoneId,
    								@PathVariable("projectId") String projectId,
    								RedirectAttributes rttr) {
  
    	try {
    		milestonesService.deleteMilestone(milestoneId,projectId);
    		rttr.addFlashAttribute("message", "마일스톤이 정상적으로 삭제되었습니다.");
            return "redirect:/project/" + projectId + "/milestones/list";
    	}catch(DataIntegrityViolationException e) {
    		rttr.addFlashAttribute("errorMessage", "해당 마일스톤에 연결된 문서가 있어 삭제할 수 없습니다.");
    		return "redirect:/project/" + projectId + "/milestones/detail?id=" + milestoneId;
    	}catch (Exception e) {
            // 3. 혹시 모를 기타 에러 대비
            rttr.addFlashAttribute("errorMessage", "마일스톤 삭제 중 알 수 없는 오류가 발생했습니다.");
            return "redirect:/project/" + projectId + "/milestones/detail";
    	}
    	
    }
    
    //목록
    @GetMapping("/list")
    public String getMilestoneList(Model model, @PathVariable("projectId") String projectId) {
       
        List<MilestonesVO> milestoneList = milestonesService.getMilestoneList(projectId);
        model.addAttribute("milestoneList", milestoneList);
        model.addAttribute("projectId", projectId);
        return "milestones/list";
    }
    
    
    
}