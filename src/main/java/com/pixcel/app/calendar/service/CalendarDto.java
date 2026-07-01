package com.pixcel.app.calendar.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {
	
	private String projectId;
	
    private String title;

    private String startDate;

    private String dueDate;

}