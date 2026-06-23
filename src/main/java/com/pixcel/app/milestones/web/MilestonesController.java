package com.pixcel.app.milestones.web;

import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;

import jakarta.servlet.http.HttpSession;
import com.pixcel.app.milestones.service.MilestoneSearchVO;
import com.pixcel.app.milestones.service.MilestonesIssueDTO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j //로그를 남겨주는 어노테이션
@Controller
@RequestMapping("/milestones")
@RequiredArgsConstructor
public class MilestonesController {

    private final MilestonesService milestonesService; //서비스를 받아 mybatis와 연결
    
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
    
    //마일스톤 생성화면
    @GetMapping("/create")
    public String createMilestoneForm(@AuthenticationPrincipal String userId,
    								HttpSession session,Model model) {
  
    String projectId = getProjectIdFromSession(session);
    
    List<MilestonesMemberDTO> managerList = milestonesService.getManagerList(projectId); //멤버 담당자 조회 테스트 향후 수정필요
    model.addAttribute("managerList", managerList); //화면에 팀원 목록을 넘겨줌
    model.addAttribute("projectId", projectId);
    return "milestones/create";
    }
    
    //생성화면 내 일감 검색
    //첫 번째 일감을 선택한 후에는 versionId가 넘어와서 해당 버전의 일감만 찾게 됩니다.
    @ResponseBody //데이터만 리턴할 때 사용 자바 객체(List)를 화면이 이해할 수 있는 JSON 문자열로 바꿔서 브라우저로 전송
    @GetMapping("/api/issues/search") //api사용 이유: JSON 같은 순수 데이터만 가져오려는 거구나
    public List<MilestonesIssueDTO>searchIssue(
    		@RequestParam("keyword")String keyword,
    		@RequestParam(value="versionId",required = false) String versionId){
    return milestonesService.getIssueList(keyword, versionId); //1. 서비스에 검색어와 버전ID를 넘겨서 조건에 맞는 일감 목록을 가져옵니다.
    }
    
    //마일스톤 데이터 실제 저장
    //화면에서 저장시 form데이터가 RequestDTO에 담겨서 컨트롤러 옴
    @PostMapping("/create")
    public String createMilestoneSubmic(
    		@ModelAttribute MilestonesVO milestoneVO,
    		HttpSession session) {
    	String projectId = getProjectIdFromSession(session);
    	milestoneVO.setProjectId(projectId);
    	log.info("마일스톤 생성 요청 데이터: {}", milestoneVO);//로그 확인용
    	String newMilestoneId = milestonesService.createMilestone(milestoneVO);
    	return "redirect:/milestones/detail?id=" +newMilestoneId;
    	
    }
    
    @GetMapping("/detail")
    public String milestoneDetail(@RequestParam("id") String milestoneId, HttpSession session, Model model) {
    	
    	String projectId = getProjectIdFromSession(session);
        // SQL JOIN 문을 통해 담당자 이름(managerName)까지 한 번에 바인딩되어 넘어옵니다.
    	MilestonesVO detailVO = milestonesService.getMilestoneDetail(milestoneId, projectId);

        if (detailVO == null) {
            return "redirect:/milestones/list"; // 조회 실패 시 메인으로 리다이렉트
        }

        model.addAttribute("milestone", detailVO);
        return "milestones/detail"; 
    }
    
 // 마일스톤 수정 화면 진입
    @GetMapping("/update")
    public String updateMilestoneForm(@RequestParam("id") String milestoneId,HttpSession session, Model model) {
    	
    	String projectId = getProjectIdFromSession(session);
    	MilestonesVO milestone = milestonesService.getMilestoneDetail(milestoneId,projectId);
        
    	if (milestone == null) {
            log.warn("권한 없는 수정 폼 접근 시도! milestoneId: {}", milestoneId);
            return "redirect:/milestones/list?error=unauthorized";
        }
    	
        List<MilestonesMemberDTO> managerList = milestonesService.getManagerList(projectId);
        List<MilestonesIssueDTO> connectedIssues = milestonesService.getConnectedIssues(milestoneId);
        // 3. 뷰(HTML)로 데이터 전달
        model.addAttribute("milestone", milestone);      // 수정 폼에 채워질 기존 데이터
        model.addAttribute("managerList", managerList);  // 담당자 선택 콤보박스 목록
        model.addAttribute("connectedIssues", connectedIssues);
        return "milestones/update"; // update.html 렌더링
    }
    
 // 마일스톤 데이터 실제 수정 (저장)
    @PostMapping("/update")
    public String updateMilestoneSubmit(
        @ModelAttribute MilestonesVO requestDTO,HttpSession session) {  
    	
    	String projectId = getProjectIdFromSession(session);
    	requestDTO.setProjectId(projectId);
        milestonesService.updateMilestone(requestDTO);
        return "redirect:/milestones/detail?id=" + requestDTO.getMilestoneId();
    }
    //삭제
    @PostMapping("/delete")
    public String deleteMilestone( String milestoneId,HttpSession session) {
    	String projectId = getProjectIdFromSession(session);
    	milestonesService.deleteMilestone(milestoneId,projectId);
    	return "redirect:/milestones/list";
    }
    
    //목록
    @GetMapping("/list")
    public String getMilestoneList(@ModelAttribute MilestoneSearchVO searchVO, Model model, HttpSession session) {
    	String projectId = getProjectIdFromSession(session);
        searchVO.setProjectId(projectId);
    	log.info("검색 조건 searchVO 확인: {}", searchVO);
        List<MilestonesVO> milestoneList = milestonesService.getMilestoneList(searchVO); //서비스(Impl)를 호출해서 마일스톤 목록 데이터를 가져옵니다.
        model.addAttribute("milestoneList", milestoneList); //화면에서 사용할 model에 데이터를 담아줌
        model.addAttribute("searchVO", searchVO); 
        return "milestones/list";
    }
    
    
    
}