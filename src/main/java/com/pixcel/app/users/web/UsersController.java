package com.pixcel.app.users.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.pixcel.app.users.service.userService;
import com.pixcel.app.users.service.userServiceVO;

import lombok.RequiredArgsConstructor;

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
    
    
    //임시 홈컨트롤 생성
    @GetMapping("/")
    public String home() {
    	return "users/home";
    }
    
    
}
