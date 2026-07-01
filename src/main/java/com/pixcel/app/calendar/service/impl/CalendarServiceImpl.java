package com.pixcel.app.calendar.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixcel.app.calendar.mapper.CalendarMapper;
import com.pixcel.app.calendar.service.CalendarDto;
import com.pixcel.app.calendar.service.CalendarService;

@Service
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private CalendarMapper mapper;

    @Override
    public List<CalendarDto> getEvents(String projectId) {

        return mapper.getEvents(projectId);

    }

}
