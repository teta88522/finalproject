package com.pixcel.app.milestones.web;

import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.milestones.service.MilestonesCreateRequestDTO;
import com.pixcel.app.milestones.service.MilestonesIssueDTO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j //로그를 남겨주는 어노테이션
@Controller
@RequestMapping("/milestones")
@RequiredArgsConstructor
public class MilestonesController {

    private final MilestonesService milestonesService; //서비스를 받아 mybatis와 연결
    
    
    //마일스톤 생성화면
    @GetMapping("/create")
    public String createMilestoneForm(Model model) {
    String testTeamId = "TEAM_202606_0001";//더미데이터 강제연결	    
    List<MilestonesMemberDTO> managerList = milestonesService.getManagerList(testTeamId); //멤버 담당자 조회 테스트 향후 수정필요
    model.addAttribute("managerList", managerList); //화면에 팀원 목록을 넘겨줌
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
    		@ModelAttribute MilestonesCreateRequestDTO requestDTO) {
    	log.info("마일스톤 생성 요청 데이터: {}", requestDTO);//로그 확인용
    	String newMilestoneId = milestonesService.createMilestone(requestDTO);
    	return "redirect:/milestones/detail?id=" +newMilestoneId;
    	
    }
    
    @GetMapping("/detail")
    public String milestoneDetail(@RequestParam("id") String milestoneId, Model model) {
        // SQL JOIN 문을 통해 담당자 이름(managerName)까지 한 번에 바인딩되어 넘어옵니다.
        MilestonesVO detailVO = milestonesService.getMilestoneDetail(milestoneId);

        if (detailVO == null) {
            return "redirect:/"; // 조회 실패 시 메인으로 리다이렉트
        }

        model.addAttribute("milestone", detailVO);
        return "milestones/detail"; 
    }
}
