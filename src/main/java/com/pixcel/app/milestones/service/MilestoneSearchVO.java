package com.pixcel.app.milestones.service;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//마일스톤 목록 필터를 담는 VO

@Getter
@Setter
@ToString
public class MilestoneSearchVO {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //json으로 데이터를 보낼떄
	@DateTimeFormat(pattern = "yyyy-MM-dd") //일반 데이터 타입 포멧 양식
    private String startDate; 

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //json으로 데이터를 보낼떄
	@DateTimeFormat(pattern = "yyyy-MM-dd") //일반 데이터 타입 포멧 양식
    private String endDate;

    private String title;
    private String managerId;
    private String managerName;
    private String statusCode;
    private String projectId;
    
}
