package com.pixcel.app.roles.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RolesController {
	
	//역할 목록 화면
	@GetMapping("/roles")
	public String rolesListForm() {
		return "roles/rolesList";
	}
	
	//역할 추가 화면
	@GetMapping("/roles/new")
	public String roleInsertForm() {
		return "roles/rolesAdd";
	} 
}
