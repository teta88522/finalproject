package com.pixcel.app.users.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.user.security.CustomUserDetails;
import com.pixcel.app.users.service.userService;
import com.pixcel.app.users.service.userServiceVO;
import com.pixcel.app.users.service.impl.MailServiceImpl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UsersController {
	
	 private final userService userService;
	 private final MailServiceImpl mailService;

	//회원가입 화면
    @GetMapping("/join")
    public String joinForm(Model model) {
    	model.addAttribute("userServiceVO",new userServiceVO());	
        return "users/usersJoin";
    }
    
    //회원가입 처리
    @PostMapping("/join")
    public String join(userServiceVO userVO) {
    	userService.joinUser(userVO);
    	return "redirect:/login";
    }
    
    //로그인
    @GetMapping("/login")
    public String loginForm() {
    	return "users/usersLogin";
    }
    
    //아이디 중복체크
    @GetMapping("/checkId")
    @ResponseBody
    public Map<String, Object> checkId(@RequestParam String loginId){
    	Map<String,Object> map = new HashMap<>();
    	
    	boolean duplicated = userService.checkLoginId(loginId);
    	map.put("duplicated", duplicated);
    	
    	return map;
    }
    
    //연락처 중복체크
    @GetMapping("/checkPhone")
    @ResponseBody
    public Map<String, Object> checkPhone(@RequestParam String phone){
    	Map<String, Object> map = new HashMap<>();
    	
    	boolean duplicated = userService.checkPhone(phone);
    	map.put("duplicated", duplicated);
    	
    	return map;
    }
    
    
    @GetMapping("/")
    public String home() {
    	return "users/usersLogin";
    }
    
    //김덕모 마이페이지
    @GetMapping("/usersMypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model){
        String userId = userDetails.getUserId();
        userServiceVO userDetail = userService.getUserDetail(userId);
        model.addAttribute("user", userDetail);

        List<ProjectVO> myProjects = userService.selectMyProjectList(userId);
    	model.addAttribute("projects", myProjects);

        return "users/usersMypage";
    }

    @PostMapping("/updateUser")
    @ResponseBody
    public  Map<String, Object> updateUser(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        userServiceVO userVO){
            userVO.setUserId(userDetails.getUserId());
    	    return userService.updateUser(userVO);
        }
    
    @PostMapping("/updatePassword")
    @ResponseBody
        public Map<String, Object> updatePassword(
    		@AuthenticationPrincipal CustomUserDetails userDetails,
    		@RequestParam String currentPassword,
    		@RequestParam String newPassword) {
        
        String userId = userDetails.getUserId();
    	return userService.updatePassword(userId, currentPassword, newPassword);
    }
    
    @PostMapping("/search")
    @ResponseBody
    public List<ProjectVO> searchProjects(
    		@RequestBody Map<String, Object> params,
    		@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 세션에서 사용자 ID 가져오기 (권한 체크용)
    	String userId = userDetails.getUserId(); 
        params.put("userId", userId);
        
        // MyBatis 호출
        return userService.getFilteredProjects(params);
    }
    
    @PostMapping("/subscribe/sendCode")
    @ResponseBody
    public Map<String, Object> sendCode(@RequestParam String email, HttpSession session){
    	Map<String, Object> response = new HashMap<>();
    	
    	try {
    		String authCode = String.format("%06d", new Random().nextInt(1000000));
    		
    		mailService.sendAuthEmail(email, authCode);
    		
    		session.setAttribute("authCode", authCode);
    		session.setAttribute("authEmail", email);
    		session.setAttribute("expireTime", LocalDateTime.now().plusMinutes(3));
    		
    		response.put("result", true);
    		response.put("message", "인증 코드가 발송되었습니다");
    		
    	}catch (Exception e) {
    		response.put("result", false);
    		response.put("message", "메일 발송에 실패했습니다: " + e.getMessage());
    	}
    	return response;
    }
    
    @PostMapping("/subscribe/verify")
    @ResponseBody
    public Map<String,Object>verifyCode(
    		@RequestParam String authCode,
    		HttpSession session){
    	Map<String, Object> response = new HashMap<>();
    	
    	String savedCode = (String) session.getAttribute("authCode");
    	LocalDateTime expireTime = (LocalDateTime) session.getAttribute("expireTime");
    	String authEmail = (String) session.getAttribute("authEmail");
    	
    	if (savedCode == null || expireTime == null) {
            response.put("result", false);
            response.put("message", "인증 정보를 찾을 수 없습니다. 다시 시도해 주세요.");
            return response;
        }
    	
    	if (LocalDateTime.now().isAfter(expireTime)) {
            response.put("result", false);
            response.put("message", "인증 시간이 만료되었습니다. 다시 발송해 주세요.");
            return response;
        }
    	
    	if (savedCode.equals(authCode)) {
            
    		userService.updateSubscribeStatus(authEmail);
            System.out.println("구독 완료된 이메일: " + authEmail);

            // 4. 사용이 끝난 세션 데이터 클리어
            session.removeAttribute("authCode");
            session.removeAttribute("expireTime");
            session.removeAttribute("authEmail");

            response.put("result", true);
            response.put("message", "구독 신청이 완료되었습니다!");
            
        } else {
            response.put("result", false);
            response.put("message", "인증번호가 일치하지 않습니다.");
        }

        return response;
    }
    
    @PostMapping("/subscribe/unsubscribe")
    public String unsubscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUserId();
        userService.unsubscribeUser(userId);
        return "redirect:/mypage";
    }
   
}
