package com.pixcel.app.testcase.web;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.test.service.TestService;
import com.pixcel.app.test.service.TestVO;
import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseService;
import com.pixcel.app.testcase.service.TestCaseVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestcasesController {

	private final TestCaseService testCaseService;
	private final TestService testService;
	 private final FileService fileService;
	

    private String getLoginUserId(String userId, String userPk) {

        if (userId != null && !userId.equals("")) {
            return userId;
        }

        if (userPk != null && !userPk.equals("")) {
            return userPk;
        }

        return null;
    }
    
    private boolean hasUploadFiles(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return false;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }

        return false;
    }
    
    //다운로드 all
    @GetMapping("/project/{projectId}/testcases/{testCaseId}/files/download-all")
    public void downloadAllTestCaseFiles(@PathVariable String projectId,
    									 @PathVariable String testCaseId,
    									 @CookieValue(value = "userId", required = false) String userId,
    									 @CookieValue(value = "user_pk", required = false) String userPk,
    									 HttpServletResponse response) throws IOException {
    	
    	String loginUserId = getLoginUserId(userId, userPk);
    	if(loginUserId == null || loginUserId.equals("")) {
    		response.sendRedirect("/login");
    		return;
    	}
    	fileService.downloadAll(testCaseId, response, loginUserId, null);
    }
    
    //다운로드 one
    @GetMapping("/project/{projectId}/testcases/files/{fileId}/download")
    public void downloadOneTestCaseFile(@PathVariable String projectId,
			 							@PathVariable String fileId,
			 							@CookieValue(value = "userId", required = false) String userId,
			 							@CookieValue(value = "user_pk", required = false) String userPk,
			 							HttpServletResponse response) throws IOException {
    	
        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            response.sendRedirect("/login");
            return;
        }

        fileService.downloadOne(fileId, response, loginUserId);
    }
    
    @GetMapping("/project/{projectId}/testcases")
    public String testCaseList(@PathVariable String projectId,
    						   TestCaseSearchVO testCaseSearchVO,
    						   @CookieValue(value = "userId", required = false) String userId,
    						   @CookieValue(value = "user_pk", required = false) String userPk,
    						   @CookieValue(value = "loginId", required = false) String loginId,
    						   Model model) {
    	String loginUserId = getLoginUserId(userId, userPk);
    	
    	if(loginUserId == null || loginUserId.equals("")) {
    		return "redirect:/login";
    	}
    	
    	testCaseSearchVO.setProjectId(projectId);
    	testCaseSearchVO.calculateSimplePaging();
    	
    	
    	List<TestCaseVO> testCaseList = testCaseService.selectTestCaseList(testCaseSearchVO);
    	
    	boolean hasNext = false;
    	if(testCaseList != null && testCaseList.size() > testCaseSearchVO.getSize()) {
    		hasNext = true;
    		testCaseList = testCaseList.subList(0, testCaseSearchVO.getSize());
    	}
    	
    	testCaseSearchVO.setHasNext(hasNext);
    	
    	List<TestVO> versionList = testService.selectProjectVersionList(projectId);
    	
    	model.addAttribute("projectId",projectId);
    	model.addAttribute("loginId", loginId);
    	model.addAttribute("testCaseSearchVO",testCaseSearchVO);
    	model.addAttribute("testCaseList",testCaseList);
    	model.addAttribute("versionList",versionList);
    	
    	return "testcase/testCaseList";
    }
    
    @GetMapping("/project/{projectId}/testcases/{testCaseId}")
    @ResponseBody
    public TestCaseVO testCaseDetail(@PathVariable String projectId,
    								 @PathVariable String testCaseId,
    								 @CookieValue(value = "userId", required = false) String userId,
    								 @CookieValue(value = "user_pk", required = false) String userPk) {
    	
    	String loginUserId = getLoginUserId(userId, userPk);
    	if(loginUserId == null || loginUserId.equals("")) {
    		return null;
    	}
    	
    	TestCaseVO testCaseVO = testCaseService.selectTestCaseDetail(projectId, testCaseId);

        if (testCaseVO != null) {
            testCaseVO.setFileList(fileService.selectAll(testCaseId, null));
        }

        return testCaseVO;
    }
	
	@GetMapping("/project/{projectId}/testcases/new")
	public String testCaseAddForm(@PathVariable String projectId,
								  @CookieValue(value = "userId", required = false) String userId,
								  @CookieValue(value ="user_pk", required = false) String userPk,
								  @CookieValue(value = "loginId", required = false) String loginId,
								  Model model) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		List<TestVO> versionList = testService.selectProjectVersionList(projectId);
		
		TestCaseVO testCaseVO = new TestCaseVO();
		testCaseVO.setStatusCode("g001");
		
		model.addAttribute("projectId", projectId);
		model.addAttribute("loginId", loginId);
		model.addAttribute("versionList", versionList);
		model.addAttribute("testCaseVO", testCaseVO);
		
		return "testcase/testCaseAdd";
	}
	
	@PostMapping("/project/{projectId}/testcases/add")
	public String insertTestCase(@PathVariable String projectId,
	                             TestCaseVO testCaseVO,
	                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
	                             @CookieValue(value = "userId", required = false) String userId,
	                             @CookieValue(value = "user_pk", required = false) String userPk) {

	    String loginUserId = getLoginUserId(userId, userPk);

	    if (loginUserId == null || loginUserId.equals("")) {
	        return "redirect:/login";
	    }

	    testCaseVO.setProjectId(projectId);
	    testCaseVO.setCreatedBy(loginUserId);

	    testCaseService.insertTestCaseWithSteps(testCaseVO);

	    if (hasUploadFiles(files)) {
	        FileDTO uploadDTO = new FileDTO();
	        uploadDTO.setProjectId(projectId);
	        uploadDTO.setVersionId(testCaseVO.getVersionId());
	        uploadDTO.setFileCode("f004");
	        uploadDTO.setUploadUserId(loginUserId);
	        uploadDTO.setConnectAddress(testCaseVO.getTestCaseId());

	        fileService.uploadFile(files, uploadDTO);
	    }

	    return "redirect:/project/" + projectId + "/testcases";
	}
	
	
	
	@GetMapping("/project/{projectId}/testcases/{testCaseId}/edit")
	public String testCaseEditForm(@PathVariable String projectId,
								   @PathVariable String testCaseId,
								   @CookieValue(value = "userId", required = false) String userId,
								   @CookieValue(value="user_pk", required = false) String userPk,
								   @CookieValue(value = "loginId", required = false) String loginId,
								   Model model) {
		
		String loginUserId = getLoginUserId(userId, userPk);
		if(loginUserId == null || loginUserId.equals("")) {
			return "redirect:/login";
		}
		
		TestCaseVO testCaseVO = testCaseService.selectTestCaseDetail(projectId, testCaseId);
		
		if(testCaseVO == null){
			return "redirect:/proejct/" + projectId + "/testcases";
		}
		
		List<TestVO> versionList = testService.selectProjectVersionList(projectId);
		List<FileVO> fileList = fileService.selectAll(testCaseId, null);
		
		model.addAttribute("projectId", projectId);
		model.addAttribute("loginId", loginId);
		model.addAttribute("versionList", versionList);
		model.addAttribute("fileList",fileList);
		model.addAttribute("testCaseVO",testCaseVO);
		
		return "testcase/testCaseEdit";
	}
	
	@PostMapping("/project/{projectId}/testcases/{testCaseId}/update")
	public String updateTestCase(@PathVariable String projectId,
	                             @PathVariable String testCaseId,
	                             TestCaseVO testCaseVO,
	                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
	                             @CookieValue(value = "userId", required = false) String userId,
	                             @CookieValue(value = "user_pk", required = false) String userPk,
	                             @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
	                             RedirectAttributes redirectAttributes) {

	    String loginUserId = getLoginUserId(userId, userPk);

	    if (loginUserId == null || loginUserId.equals("")) {
	        return "redirect:/login";
	    }
	    
	    
	    //관리자 여부 확인
	    boolean isAdmin = subscribeYn != null && subscribeYn.equalsIgnoreCase("Y");
	    
	    if(!isAdmin) {
	    	TestCaseVO ownerCheckVO = new TestCaseVO();
	    	ownerCheckVO.setTestCaseId(testCaseId);
	    	ownerCheckVO.setCreatedBy(loginUserId);
	    	
	    	int ownerCount = testCaseService.checkTestCaseOwner(ownerCheckVO);
	    	
	    	if(ownerCount == 0) {
	    		redirectAttributes.addFlashAttribute("message","수정권한이 없습니다.");
	    		return "redirect:/project/" + projectId + "/testcases";
	    	}
	    }

	    testCaseVO.setProjectId(projectId);
	    testCaseVO.setTestCaseId(testCaseId);

	    testCaseService.updateTestCaseWithSteps(testCaseVO);

	    if (hasUploadFiles(files)) {
	        FileDTO uploadDTO = new FileDTO();
	        uploadDTO.setProjectId(projectId);
	        uploadDTO.setVersionId(testCaseVO.getVersionId());
	        uploadDTO.setFileCode("f004");
	        uploadDTO.setUploadUserId(loginUserId);
	        uploadDTO.setConnectAddress(testCaseId);

	        fileService.uploadFile(files, uploadDTO);
	    }

	    return "redirect:/project/" + projectId + "/testcases";
	}
}
