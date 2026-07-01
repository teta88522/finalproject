package com.pixcel.app.test.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.test.service.TestGroupVO;
import com.pixcel.app.test.service.TestSearchVO;
import com.pixcel.app.test.service.TestService;
import com.pixcel.app.test.service.TestSummaryVO;
import com.pixcel.app.test.service.TestUserVO;
import com.pixcel.app.test.service.TestVO;
import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseService;
import com.pixcel.app.testcase.service.TestCaseVO;
import com.pixcel.app.testcase.web.TestcasesController;
import com.pixcel.app.testexecution.service.TestExecutionService;
import com.pixcel.app.testexecution.service.TestExecutionVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/{projectId}/tests")
public class TestController {
	
	private final TestcasesController testcasesController;
	private final TestService testService;
	private final TestCaseService testCaseService;
	private final TestExecutionService testExecutionService;

	
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

        long start = System.currentTimeMillis();

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        if (loginId == null || loginId.equals("")) {
            loginId = loginUserId;
        }

        testSearchVO.setProjectId(projectId);
        testSearchVO.setLoginUserId(loginUserId);
        testSearchVO.setSubscribeYn(subscribeYn);

        model.addAttribute("projectId", projectId);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("loginId", loginId);
        model.addAttribute("subscribeYn", subscribeYn);
        model.addAttribute("testSearchVO", testSearchVO);

        boolean isAdmin = "Y".equals(subscribeYn);

        long asyncStart = System.currentTimeMillis();

        CompletableFuture<TestSummaryVO> summaryFuture;
        CompletableFuture<List<TestVO>> listFuture;

        if (isAdmin) {
            summaryFuture = CompletableFuture.supplyAsync(() -> testService.selectAdminTestSummary(testSearchVO));
            listFuture = CompletableFuture.supplyAsync(() -> testService.selectAdminTestList(testSearchVO));
        } else {
            summaryFuture = CompletableFuture.supplyAsync(() -> testService.selectUserTestSummary(testSearchVO));
            listFuture = CompletableFuture.supplyAsync(() -> testService.selectUserTestList(testSearchVO));
        }

        CompletableFuture.allOf(summaryFuture, listFuture).join();

        TestSummaryVO testSummary = summaryFuture.join();
        List<TestVO> testList = listFuture.join();

        System.out.println("test list async query total = " + (System.currentTimeMillis() - asyncStart) + "ms");

        model.addAttribute("testSummary", testSummary);
        model.addAttribute("testList", testList);

        System.out.println("TOTAL test list page = " + (System.currentTimeMillis() - start) + "ms");

        if (isAdmin) {
            return "test/testAdminList";
        }

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

	    long start = System.currentTimeMillis();

	    String loginUserId = getLoginUserId(userId, userPk);

	    if (loginUserId == null || loginUserId.equals("")) {
	        return "redirect:/login";
	    }

	    if (loginId == null || loginId.equals("")) {
	        loginId = loginUserId;
	    }

	    testCaseSearchVO.setProjectId(projectId);

	    if (testCaseSearchVO.getStatusCode() == null || testCaseSearchVO.getStatusCode().equals("")) {
	        testCaseSearchVO.setStatusCode("g001");
	    }

	    testCaseSearchVO.calculateSimplePaging();

	    long asyncStart = System.currentTimeMillis();

	    CompletableFuture<List<TestCaseVO>> testCaseListFuture =
	            CompletableFuture.supplyAsync(() -> testCaseService.selectTestCaseList(testCaseSearchVO));

	    CompletableFuture<List<TestVO>> versionListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectProjectVersionList(projectId));

	    CompletableFuture<List<TestUserVO>> assigneeListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectTestAssigneeList(projectId));

	    CompletableFuture<List<TestGroupVO>> groupListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectProjectGroupList(projectId));

	    CompletableFuture.allOf(
	            testCaseListFuture,
	            versionListFuture,
	            assigneeListFuture,
	            groupListFuture
	    ).join();

	    List<TestCaseVO> testCaseList = testCaseListFuture.join();
	    List<TestVO> versionList = versionListFuture.join();
	    List<TestUserVO> assigneeList = assigneeListFuture.join();
	    List<TestGroupVO> groupList = groupListFuture.join();

	    System.out.println("test add async query total = " + (System.currentTimeMillis() - asyncStart) + "ms");

	    boolean hasNext = false;

	    if (testCaseList != null && testCaseList.size() > testCaseSearchVO.getSize()) {
	        hasNext = true;
	        testCaseList = testCaseList.subList(0, testCaseSearchVO.getSize());
	    }

	    testCaseSearchVO.setHasNext(hasNext);

	    TestVO testVO = new TestVO();
	    testVO.setProjectId(projectId);
	    testVO.setStatusCode("a001");
	    testVO.setPriorityCode("c002");
	    testVO.setTestTypeCode("d001");
	    testVO.setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
	    testVO.setEndDate(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

	    model.addAttribute("projectId", projectId);
	    model.addAttribute("loginUserId", loginUserId);
	    model.addAttribute("loginId", loginId);
	    model.addAttribute("subscribeYn", subscribeYn);

	    model.addAttribute("testVO", testVO);
	    model.addAttribute("versionList", versionList);
	    model.addAttribute("assigneeList", assigneeList);

	    model.addAttribute("testCaseSearchVO", testCaseSearchVO);
	    model.addAttribute("testCaseList", testCaseList);
	    model.addAttribute("groupList", groupList);

	    System.out.println("TOTAL test add page = " + (System.currentTimeMillis() - start) + "ms");

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
	
	@GetMapping("/{testId}/edit")
	public String testEditForm(@PathVariable String projectId,
	                           @PathVariable String testId,
	                           TestCaseSearchVO testCaseSearchVO,
	                           @CookieValue(value = "userId", required = false) String userId,
	                           @CookieValue(value = "user_pk", required = false) String userPk,
	                           @CookieValue(value = "loginId", required = false) String loginId,
	                           @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                           RedirectAttributes redirectAttributes,
	                           Model model) {

	    long start = System.currentTimeMillis();

	    String loginUserId = getLoginUserId(userId, userPk);

	    if (loginUserId == null || loginUserId.equals("")) {
	        return "redirect:/login";
	    }

	    TestVO testVO = testService.selectTestDetail(projectId, testId);

	    if (testVO == null) {
	        redirectAttributes.addFlashAttribute("message", "존재하지 않는 테스트입니다.");
	        return "redirect:/project/" + projectId + "/tests";
	    }

	    boolean isAdmin = subscribeYn != null && subscribeYn.equalsIgnoreCase("Y");

//	    // 관리자가 아니면 작성자 본인인지 확인
//	    // checkTestOwner()를 다시 호출하지 않고 selectTestDetail 결과의 createdBy로 체크
//	    if (!isAdmin) {
//	        if (testVO.getCreatedBy() == null || !testVO.getCreatedBy().equals(loginUserId)) {
//	            redirectAttributes.addFlashAttribute("message", "수정 권한이 없습니다.");
//	            return "redirect:/project/" + projectId + "/tests";
//	        }
//	    }

	    testCaseSearchVO.setProjectId(projectId);

	    if (testCaseSearchVO.getStatusCode() == null || testCaseSearchVO.getStatusCode().equals("")) {
	        testCaseSearchVO.setStatusCode("g001");
	    }

	    testCaseSearchVO.calculateSimplePaging();

	    long asyncStart = System.currentTimeMillis();

	    CompletableFuture<List<TestCaseVO>> testCaseListFuture =
	            CompletableFuture.supplyAsync(() -> testCaseService.selectTestCaseList(testCaseSearchVO));

	    CompletableFuture<List<TestCaseVO>> selectedCaseListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectMappedTestCaseList(testId));

	    CompletableFuture<List<TestVO>> versionListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectProjectVersionList(projectId));

	    CompletableFuture<List<TestUserVO>> assigneeListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectTestAssigneeList(projectId));

	    CompletableFuture<List<TestGroupVO>> groupListFuture =
	            CompletableFuture.supplyAsync(() -> testService.selectProjectGroupList(projectId));

	    CompletableFuture.allOf(
	            testCaseListFuture,
	            selectedCaseListFuture,
	            versionListFuture,
	            assigneeListFuture,
	            groupListFuture
	    ).join();

	    List<TestCaseVO> testCaseList = testCaseListFuture.join();
	    List<TestCaseVO> selectedCaseList = selectedCaseListFuture.join();
	    List<TestVO> versionList = versionListFuture.join();
	    List<TestUserVO> assigneeList = assigneeListFuture.join();
	    List<TestGroupVO> groupList = groupListFuture.join();

	    System.out.println("2~6. async query total = " + (System.currentTimeMillis() - asyncStart) + "ms");

	    boolean hasNext = false;

	    if (testCaseList != null && testCaseList.size() > testCaseSearchVO.getSize()) {
	        hasNext = true;
	        testCaseList = testCaseList.subList(0, testCaseSearchVO.getSize());
	    }

	    testCaseSearchVO.setHasNext(hasNext);

	    model.addAttribute("projectId", projectId);
	    model.addAttribute("testId", testId);
	    model.addAttribute("loginId", loginId);

	    model.addAttribute("testVO", testVO);
	    model.addAttribute("testCaseSearchVO", testCaseSearchVO);
	    model.addAttribute("testCaseList", testCaseList);
	    model.addAttribute("selectedCaseList", selectedCaseList);
	    model.addAttribute("versionList", versionList);
	    model.addAttribute("assigneeList", assigneeList);
	    model.addAttribute("groupList", groupList);

	    System.out.println("TOTAL edit page = " + (System.currentTimeMillis() - start) + "ms");

	    return "test/testEdit";
	}
	
	@PostMapping("/{testId}/update")
	public String updateTest(@PathVariable String projectId,
						     @PathVariable String testId,
						     TestVO testVO,
						     @CookieValue(value = "userId", required = false) String userId,
						     @CookieValue(value = "user_pk", required = false) String userPk,
						     @CookieValue(value= "subscribeYn", required = false) String subscribeYn,
						     RedirectAttributes redirectAttributes) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		boolean isAdmin = subscribeYn != null && subscribeYn.equalsIgnoreCase("Y");
		if(!isAdmin) {
			TestVO ownerCheckVO = new TestVO();
			ownerCheckVO.setTestId(testId);
			ownerCheckVO.setCreatedBy(loginUserId);
			
			int ownerCount = testService.checkTestOwner(ownerCheckVO);
			
			if(ownerCount == 0) {
				redirectAttributes.addFlashAttribute("message","수정 권한이 없습니다.");
				return "redirect:/project/" + projectId + "/tests";
			}
		}
		testVO.setTestId(testId);
		
		if(testVO.getTestTypeCode() == null || testVO.getTestTypeCode().equals("")) {
			testVO.setTestTypeCode("d001");
		}
		if(testVO.getStatusCode() == null || testVO.getStatusCode().equals("")) {
			testVO.setStatusCode("a002");
		}
		if(testVO.getStartDate() == null || testVO.getStartDate().equals("")) {
			testVO.setStartDate(java.time.LocalDate.now().toString());
		}
		if(testVO.getEndDate() == null || testVO.getEndDate().equals("")) {
			testVO.setEndDate(java.time.LocalDateTime.now().toString());
		}
		if(testVO.getTestCaseId() == null || testVO.getTestCaseId().equals("")) {
			redirectAttributes.addFlashAttribute("message","테스트케이스를 1개 이상 선택하세요.");
			return "redirect:/project/" + projectId + "/tests/" + testId + "/edit";
		}
		
		testService.updateTestWithCases(testVO);
		redirectAttributes.addFlashAttribute("message","테스트가 수정되었습니다.");
		
		return "redirect:/project/" + projectId + "/tests";
	}
	
	
	@GetMapping("/{testId}")
	public String testDetail(@PathVariable String projectId,
							 @PathVariable String testId,
							 @CookieValue(value="userId", required = false) String userId,
							 @CookieValue(value="user_pk", required = false) String userPk,
							 @CookieValue(value="loginId", required = false) String loginId,
							 @CookieValue(value="subscribeYn", required = false) String subscribeYn,
							 RedirectAttributes redirectAttributes,
							 Model model) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		TestExecutionVO testInfo = testExecutionService.selectTestExecutionInfo(projectId, testId);
		
		if(testInfo == null) {
			redirectAttributes.addFlashAttribute("message","존재하지 않는 테스트입니다.");
			return "redirect:/project/" + projectId + "/tests";
		}
		
		List<TestExecutionVO> executionCaseList = testExecutionService.selectExecutionCaseList(testId);
		
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("testId", testId);
	    model.addAttribute("loginUserId", loginUserId);
	    model.addAttribute("loginId", loginId);
	    model.addAttribute("subscribeYn", subscribeYn);

	    model.addAttribute("testInfo", testInfo);
	    model.addAttribute("executionCaseList", executionCaseList);
		
		return "test/testDetail";
	}
	
	@PostMapping("/{testId}/executions/save")
	public String saveTestExecution(@PathVariable String projectId,
							        @PathVariable String testId,
							        TestExecutionVO testExecutionVO,
							        @CookieValue(value = "userId", required = false) String userId,
							        @CookieValue(value = "user_pk", required = false) String userPk,
							        RedirectAttributes redirectAttributes) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		testExecutionVO.setExecutorId(loginUserId);
		
		if(testExecutionVO.getResultStatusCode() == null || testExecutionVO.getResultStatusCode().equals("")) {
			testExecutionVO.setResultStatusCode("o001");
		}
	    if (testExecutionVO.getMappingId() == null || testExecutionVO.getMappingId().equals("")) {
	        redirectAttributes.addFlashAttribute("message", "테스트케이스 매핑 정보가 없습니다.");
	        return "redirect:/project/" + projectId + "/tests/" + testId;
	    }
	    
	    testExecutionService.insertTestExecution(testExecutionVO);
	    redirectAttributes.addFlashAttribute("message", "테스트 실행 결과가 저장되었습니다.");
	    
		return "redirect:/project/" + projectId + "/tests/" + testId;
	}
	
	@PostMapping("/{testId}/executions/save-all")
	public String saveAllTestExecution(@PathVariable String projectId,
									   @PathVariable String testId,
									   TestExecutionVO testExecutionVO,
									   @CookieValue(value = "userId", required = false) String userId,
									   @CookieValue(value="user_pk", required = false) String userPk,
									   RedirectAttributes redirectAttributes) {
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		 testExecutionVO.setExecutorId(loginUserId);

		 if (testExecutionVO.getMappingIdList() == null || testExecutionVO.getMappingIdList().isEmpty()) {
		        redirectAttributes.addFlashAttribute("message", "저장할 테스트 실행결과가 없습니다.");
		        return "redirect:/project/" + projectId + "/tests/" + testId;
		 }

		 testExecutionService.insertTestExecutionList(testExecutionVO);

		 redirectAttributes.addFlashAttribute("message", "테스트 실행결과가 저장되었습니다.");

		 return "redirect:/project/" + projectId + "/tests/" + testId;
		
	}
	
	@PostMapping("/{testId}/executions/save-ajax")
	@ResponseBody
	public TestExecutionVO saveTestExecutionAjax(@PathVariable String projectId,
	                                             @PathVariable String testId,
	                                             TestExecutionVO testExecutionVO,
	                                             @CookieValue(value = "userId", required = false) String userId,
	                                             @CookieValue(value = "user_pk", required = false) String userPk) {

	    String loginUserId = getLoginUserId(userId, userPk);

	    if (loginUserId == null || loginUserId.equals("")) {
	        TestExecutionVO result = new TestExecutionVO();
	        result.setResultStatusCode("LOGIN_REQUIRED");
	        result.setResultStatusName("로그인이 필요합니다.");
	        return result;
	    }

	    if (testExecutionVO.getMappingId() == null || testExecutionVO.getMappingId().equals("")) {
	        TestExecutionVO result = new TestExecutionVO();
	        result.setResultStatusCode("ERROR");
	        result.setResultStatusName("테스트케이스 매핑 정보가 없습니다.");
	        return result;
	    }

	    testExecutionVO.setExecutorId(loginUserId);

	    if (testExecutionVO.getResultStatusCode() == null || testExecutionVO.getResultStatusCode().equals("")) {
	        testExecutionVO.setResultStatusCode("o001");
	    }

	    testExecutionService.insertTestExecution(testExecutionVO);

	    TestExecutionVO result = new TestExecutionVO();
	    result.setMappingId(testExecutionVO.getMappingId());
	    
	    result.setExecutionId(testExecutionVO.getExecutionId());
	    result.setResultStatusCode(testExecutionVO.getResultStatusCode());

	    if ("o001".equals(testExecutionVO.getResultStatusCode())) {
	        result.setResultStatusName("미수행");
	    } else if ("o002".equals(testExecutionVO.getResultStatusCode())) {
	        result.setResultStatusName("진행중");
	    } else if ("o003".equals(testExecutionVO.getResultStatusCode())) {
	        result.setResultStatusName("성공");
	    } else if ("o004".equals(testExecutionVO.getResultStatusCode())) {
	        result.setResultStatusName("실패");
	    } else {
	        result.setResultStatusName(testExecutionVO.getResultStatusCode());
	    }

	    return result;
	}
}
