package com.pixcel.app.gantt.service;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GanttTaskDTO {

	private String id;
	private String text;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate start_date;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate end_date;
	private Integer duration;
	private String type;
	private String parent;
	private Double progress;
	private String issueStatusName;
	private String assigneeName;
}
