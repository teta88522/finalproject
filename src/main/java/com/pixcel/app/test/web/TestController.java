package com.pixcel.app.test.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pixcel.app.test.service.TestGroupVO;
import com.pixcel.app.test.service.TestSearchVO;
import com.pixcel.app.test.service.TestService;
import com.pixcel.app.test.service.TestSummaryVO;
import com.pixcel.app.test.service.TestUserVO;
import com.pixcel.app.test.service.TestVO;
import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseService;
import com.pixcel.app.testcase.service.TestCaseVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/{projectId}/tests")
public class TestController {
	
	private final TestService testService;
	private final TestCaseService testCaseService;
	
    private String getLoginUserId(String userId, String userPk) {

        if (userId != null && !userId.equals("")) {
            return userId;
        }

        if (userPk != null && !userPk.equals("")) {
            return userPk;
        }

        return null;
    }
	
	@GetMapping
	public String testList(@PathVariable String projectId,
						   @ModelAttribute TestSearchVO testSearchVO,
						   @CookieValue(value = "userId", required = false) String userId,
						   @CookieValue(value = "user_pk", required = false) String userPk,
						   @CookieValue(value = "loginId", required = false) String loginId,
						   @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
						   Model model) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		if(loginId == null || loginId.equals("")) {
			loginId = loginUserId;
		}
		
		testSearchVO.setProjectId(projectId);
		testSearchVO.setLoginUserId(loginUserId);
		testSearchVO.setSubscribeYn(subscribeYn);
		
		model.addAttribute("projectId",projectId);
		model.addAttribute("loginUserId", loginUserId);
		model.addAttribute("loginId",loginId);
		model.addAttribute("subscribeYn", subscribeYn);
		model.addAttribute("testSearchVO", testSearchVO);
		
		
		if("Y".equals(subscribeYn)) {
			TestSummaryVO testSummary = testService.selectAdminTestSummary(testSearchVO);
			List<TestVO> testList = testService.selectAdminTestList(testSearchVO);
			
			model.addAttribute("testSummary", testSummary);
			model.addAttribute("testList", testList);
			
			return "test/testAdminList";
		}
		
		TestSummaryVO testSummary = testService.selectUserTestSummary(testSearchVO);
		List<TestVO> testList = testService.selectUserTestList(testSearchVO);
		
		model.addAttribute("testSummary", testSummary);
		model.addAttribute("testList", testList);
	
		return "test/testUserList";
	}
	
	@GetMapping("/new")
	public String testAddForm(@PathVariable String projectId,
							  @ModelAttribute TestCaseSearchVO testCaseSearchVO,
							  @CookieValue(value = "userId", required = false) String userId,
							  @CookieValue(value = "user_pk", required = false) String userPk,
							  @CookieValue(value = "loginId", required = false) String loginId,
							  @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
							  Model model) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		if(loginId == null || loginId.equals("")) {
			return "redirect:/login";
		}
		
		testCaseSearchVO.setProjectId(projectId);
		
		//페이징 계산
		int totalCount = testCaseService.selectTestCaseCount(testCaseSearchVO);
		testCaseSearchVO.setTotalCount(totalCount);
		testCaseSearchVO.calculatePagin();
		
		
		
		List<TestVO> versionList = testService.selectProjectVersionList(projectId);
		List<TestUserVO> assigneeList = testService.selectTestAssigneeList(projectId);
		List<TestCaseVO> testCaseList = testCaseService.selectTestCaseList(testCaseSearchVO);
		List<TestGroupVO> groupList = testService.selectProjectGroupList(projectId);
		
		
		TestVO testVO = new TestVO();
		testVO.setProjectId(projectId);
		testVO.setStatusCode("a001");
		testVO.setPriorityCode("c002");
		testVO.setTestTypeCode("d001");
        testVO.setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        testVO.setEndDate(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        model.addAttribute("projectId",projectId);
        model.addAttribute("loginUserId",loginUserId);
        model.addAttribute("loginId",loginId);
        model.addAttribute("subscribeYn",subscribeYn);
        
        model.addAttribute("testVO",testVO);
        model.addAttribute("versionList",versionList);
        model.addAttribute("assigneeList",assigneeList);
        
        model.addAttribute("testCaseSearchVO", testCaseSearchVO);
        model.addAttribute("testCaseList", testCaseList);
        model.addAttribute("groupList",groupList);
        
		return "test/testAdd";
	}
	
	
	@PostMapping("/add")
	public String insertTest(@PathVariable String projectId,
							 @ModelAttribute TestVO testVO,
							 @CookieValue(value = "userId", required = false) String userId,
							 @CookieValue(value = "user_pk", required = false) String userPk) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		testVO.setProjectId(projectId);
		testVO.setCreatedBy(loginUserId);
		
		testService.insertTestWithCases(testVO);
		
		return "redirect:/project/" + projectId + "/tests";
	}
	
	
}
