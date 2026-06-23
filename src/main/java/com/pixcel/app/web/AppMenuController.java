package com.pixcel.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class AppMenuController {
	
	@GetMapping("/dashboard/list")
	public String dashboardList(HttpSession session) {

	    session.setAttribute("menu", "project");

	    return "dashboard/list";
	}
	
	@GetMapping("/admin/list")
	public String adminList(HttpSession session) {

	    session.setAttribute("menu", "admin");

	    return "codevalue/list";
	}
}
