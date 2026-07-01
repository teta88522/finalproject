package com.pixcel.app.calendar.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pixcel.app.calendar.service.CalendarDto;
import com.pixcel.app.calendar.service.CalendarService;
import com.pixcel.app.web.AllProjectController;

import lombok.RequiredArgsConstructor;

@AllProjectController
@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalenderController {
	@Autowired
	private CalendarService calendarService;
	
	@GetMapping("/view")
    public String calendar(@PathVariable("projectId") String projectId, Model model) {
		model.addAttribute("projectId",projectId);
        return "calendar/calendar";

    }
	

    @GetMapping("/events")
    @ResponseBody
    public List<CalendarDto> getEvents(@PathVariable("projectId") String projectId){

        return calendarService.getEvents(projectId);

    }

}
