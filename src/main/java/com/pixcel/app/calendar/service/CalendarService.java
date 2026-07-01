package com.pixcel.app.calendar.service;

import java.util.List;

public interface CalendarService {
	List<CalendarDto> getEvents(String projectId);
}
