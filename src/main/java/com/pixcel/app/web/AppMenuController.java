package com.pixcel.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppMenuController {
	
	@GetMapping("/dashboard/list")
	public String dashboardList(Model model) {

	    model.addAttribute("menuType", "project");

	    return "project/projectList";
	}
	@GetMapping("/admin/list")
	public String adminList(Model model) {

	    model.addAttribute("menuType", "admin");

	    return "codevalue/list";
	}
	
	@GetMapping("/teams/list")
	public String teamList(Model model) {

	    model.addAttribute("menuType", "team");

	    return "team/list";
	}
}
