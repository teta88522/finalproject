package com.pixcel.app.calendar.mapper;

import java.util.List;

import com.pixcel.app.calendar.service.CalendarDto;

public interface CalendarMapper {

    List<CalendarDto> getEvents(String projectId);

}
