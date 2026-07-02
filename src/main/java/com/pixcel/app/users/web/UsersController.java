package com.pixcel.app.users.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.user.security.CustomUserDetails;
import com.pixcel.app.users.service.userService;
import com.pixcel.app.users.service.userServiceVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequiredArgsConstructor
public class UsersController {
	
	 private final userService userService;

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
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model){
        String userId = userDetails.getUserId();
        userServiceVO userDetail = userService.getUserDetail(userId);
        model.addAttribute("user", userDetail);

        List<ProjectVO> myProjects = userService.selectMyProjectList(userId);
    	model.addAttribute("projects", myProjects);

        return "users/usersMyPage";
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
    
   
}
