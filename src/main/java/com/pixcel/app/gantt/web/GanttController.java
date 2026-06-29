package com.pixcel.app.gantt.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.pixcel.app.gantt.service.GanttResponse;
import com.pixcel.app.gantt.service.GanttService;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/gantt")
@RequiredArgsConstructor
@AllProjectController
public class GanttController {
	
	private final GanttService ganttService;
	
	@GetMapping("/ganttView")
	public String ganttChartView(@PathVariable("projectId") String projectId, Model model) {
		model.addAttribute("projectId",projectId);
		
		return "gantt/ganttView";
	}
	
	@ResponseBody
	@GetMapping("/api/data")
	public GanttResponse getGanttData(@PathVariable("projectId")String projectId) {
		return ganttService.getGanttChartData(projectId);
	}
}
