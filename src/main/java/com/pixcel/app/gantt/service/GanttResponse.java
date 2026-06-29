package com.pixcel.app.gantt.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GanttResponse {
	private List<GanttTaskDTO> data;
	private List<?> links;
}
