package com.pixcel.app.bug.web;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.bug.service.BugSearchVO;
import com.pixcel.app.bug.service.BugService;
import com.pixcel.app.bug.service.BugVO;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;
    private final FileService fileService;
    
    private boolean hasUploadFiles(List<MultipartFile> files) {
    	
    	if(files == null || files.isEmpty()) {
    		return false;
    	}
    	
    	for(MultipartFile file : files) {
    		if(file != null && !file.isEmpty()) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    private String getLoginUserId(String userId, String userPk) {

        if (userId != null && !userId.equals("")) {
            return userId;
        }

        if (userPk != null && !userPk.equals("")) {
            return userPk;
        }

        return null;
    }
    
    // 전체 다운로드
    @GetMapping("/project/{projectId}/bugs/{bugId}/files/download-all")
    public void downloadAllBugFiles(@PathVariable String projectId,
    								@PathVariable String bugId,
    								@CookieValue(value = "userId", required = false) String userId,
    								@CookieValue(value = "user_pk", required = false) String userPk,
    								HttpServletResponse response) throws IOException {
    	
    	String loginUserId = getLoginUserId(userId, userPk);
    	if(loginUserId == null || loginUserId.equals("")) {
    		response.sendRedirect("/login");
    		return;
    	}
    	
    	fileService.downloadAll(bugId, response, loginUserId, null);
    	
    }
    
    // 단건 다운로드
    @GetMapping("/project/{projectId}/bugs/files/{fileId}/download")
    public void downloadOneBugFile(@PathVariable String projectId,
    							   @PathVariable String fileId,
    							   @CookieValue(value = "userId", required = false) String userId,
    							   @CookieValue(value = "user_pk", required = false) String userPk,
    							   HttpServletResponse response) throws IOException {
    	
    	String loginUserId = getLoginUserId(userId, userPk);
    	if(loginUserId == null || loginUserId.equals("")) {
    		response.sendRedirect("/login");
    		return;
    	}
    	
    	fileService.downloadOne(fileId, response, loginUserId);
    	
    }

    // 버그 목록
    @GetMapping("/project/{projectId}/bugs")
    public String bugList(@PathVariable String projectId,
                          @ModelAttribute BugSearchVO searchVO,
                          Model model,
                          @CookieValue(value = "userId", required = false) String userId,
                          @CookieValue(value = "user_pk", required = false) String userPk,
                          @CookieValue(value = "subscribeYn", required = false) String subscribeYn) {

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        searchVO.setProjectId(projectId);

        List<BugVO> bugList = bugService.selectBugList(searchVO);

        model.addAttribute("bugList", bugList);
        model.addAttribute("searchVO", searchVO);
        model.addAttribute("projectId", projectId);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("subscribeYn", subscribeYn);
        model.addAttribute("isAdmin", "Y".equals(subscribeYn));

        return "bug/bugList";
    }

    // 버그 등록화면
    @GetMapping("/project/{projectId}/bugs/new")
    public String bugAddForm(@PathVariable String projectId,
                             @RequestParam(value = "executionId", required = false) String executionId,
                             Model model,
                             @CookieValue(value = "userId", required = false) String userId,
                             @CookieValue(value = "user_pk", required = false) String userPk,
                             @CookieValue(value="userName", required = false) String userName) {


        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        BugVO bugVO = new BugVO();

        if (executionId != null && !executionId.equals("")) {
            BugVO baseInfo = bugService.selectBugAddInfoByExecutionId(executionId);

            if (baseInfo != null) {
                bugVO = baseInfo;
                bugVO.setFoundVersion(bugVO.getVersionName());
            }
        }

        bugVO.setExecutionId(executionId);
        bugVO.setReporterId(loginUserId);
        
        if(userName != null && !userName.equals("")) {
        	bugVO.setReporterName(userName);
        	System.out.println(bugVO.getReporterName());
        }

        if (bugVO.getBugStatusCode() == null || bugVO.getBugStatusCode().equals("")) {
            bugVO.setBugStatusCode("p001"); // 진행중
        }

        if (bugVO.getSeverityCode() == null || bugVO.getSeverityCode().equals("")) {
            bugVO.setSeverityCode("q003"); // 보통
        }

        if (bugVO.getPriorityCode() == null || bugVO.getPriorityCode().equals("")) {
            bugVO.setPriorityCode("c002"); // 중
        }

        if (bugVO.getBugTypeCode() == null || bugVO.getBugTypeCode().equals("")) {
            bugVO.setBugTypeCode("d001"); // 기능
        }

        if (bugVO.getOccurredAtText() == null || bugVO.getOccurredAtText().equals("")) {
            bugVO.setOccurredAtText(java.time.LocalDate.now().toString());
        }

        model.addAttribute("bugVO", bugVO);
        model.addAttribute("projectId", projectId);
        model.addAttribute("loginUserId", loginUserId);

        return "bug/bugAdd";
    }

    // 버그 등록 처리
    @PostMapping("/project/{projectId}/bugs/add")
    public String insertBug(@PathVariable String projectId,
                            @ModelAttribute BugVO bugVO,
                            @RequestParam(value = "files", required = false) List<MultipartFile> files,
                            @CookieValue(value = "userId", required = false) String userId,
                            @CookieValue(value = "user_pk", required = false) String userPk,
                            Model model) {

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        if (bugVO.getExecutionId() == null || bugVO.getExecutionId().equals("")) {
            model.addAttribute("message", "테스트 수행 결과 정보가 없습니다.");
            model.addAttribute("bugVO", bugVO);
            model.addAttribute("projectId", projectId);
            model.addAttribute("loginUserId", loginUserId);
            return "bug/bugAdd";
        }
       
        bugVO.setReporterId(loginUserId);

        bugService.insertBug(bugVO);
        
        if(hasUploadFiles(files)) {
        	FileDTO uploadDTO = new FileDTO();
        	uploadDTO.setProjectId(projectId);
        	uploadDTO.setVersionId(bugVO.getVersionId());
        	
        	uploadDTO.setFileCode("f006");
        	uploadDTO.setUploadUserId(loginUserId);
        	
        	uploadDTO.setConnectAddress(bugVO.getBugId());
        	fileService.uploadFile(files, uploadDTO);
        }
        


        return "redirect:/project/" + projectId + "/bugs";
    }

    // 버그 수정화면
    @GetMapping("/project/{projectId}/bugs/{bugId}")
    public String bugUpdateForm(@PathVariable String projectId,
                                @PathVariable String bugId,
                                Model model,
                                @CookieValue(value = "userId", required = false) String userId,
                                @CookieValue(value = "user_pk", required = false) String userPk,
                                @CookieValue(value = "subscribeYn", required = false) String subscribeYn) {

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        BugVO bugVO = bugService.selectBugDetail(bugId);

        if (bugVO == null) {
            return "redirect:/project/" + projectId + "/bugs";
        }

        boolean isAdmin = "Y".equals(subscribeYn);
        boolean isWriter = loginUserId.equals(bugVO.getReporterId());

        if (!isAdmin && !isWriter) {
            return "redirect:/project/" + projectId + "/bugs";
        }

        model.addAttribute("bugVO", bugVO);
        model.addAttribute("projectId", projectId);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("isAdmin", isAdmin);

        return "bug/bugUpdate";
    }

    // 버그 수정 처리
    @PostMapping("/project/{projectId}/bugs/update")
    public String updateBug(@PathVariable String projectId,
                            @ModelAttribute BugVO bugVO,
                            @CookieValue(value = "userId", required = false) String userId,
                            @CookieValue(value = "user_pk", required = false) String userPk,
                            @CookieValue(value = "subscribeYn", required = false) String subscribeYn) {

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        bugVO.setLoginUserId(loginUserId);

        if ("Y".equals(subscribeYn)) {
            bugService.updateBug(bugVO);
        } else {
            bugService.updateBugByReporter(bugVO);
        }

        return "redirect:/project/" + projectId + "/bugs";
    }

    // 버그 삭제 처리
    @PostMapping("/project/{projectId}/bugs/delete")
    public String deleteBug(@PathVariable String projectId,
                            @ModelAttribute BugVO bugVO,
                            @CookieValue(value = "userId", required = false) String userId,
                            @CookieValue(value = "user_pk", required = false) String userPk,
                            @CookieValue(value = "subscribeYn", required = false) String subscribeYn) {

        String loginUserId = getLoginUserId(userId, userPk);

        if (loginUserId == null || loginUserId.equals("")) {
            return "redirect:/login";
        }

        bugVO.setLoginUserId(loginUserId);

        if ("Y".equals(subscribeYn)) {
            bugService.delete(bugVO.getBugId());
        } else {
            bugService.deleteBugByReporter(bugVO);
        }

        return "redirect:/project/" + projectId + "/bugs";
    }
}