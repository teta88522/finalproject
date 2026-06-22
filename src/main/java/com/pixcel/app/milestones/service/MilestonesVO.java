package com.pixcel.app.milestones.service;


import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * 테이블 명, 컬럼 명, PK 지정, 연관관계 설정 등 구조 선언
 * */

@Getter
@Setter			   										//MyBatis 매핑을 위해 필요
@ToString
@Builder
@NoArgsConstructor 										//Mybatis가 객체를 생성할 때 필요함
@AllArgsConstructor										//데이터가 있는 객체를 한번에 만들때 좋음
public class MilestonesVO {
	private String milestoneId;  
	private String versionId;    
    private String title;        
    private String description;  
    private String managerId;    
    private String statusCode;   
    private String statusName; 
    private LocalDate startDate; 
    private LocalDate endDate;  
    private LocalDateTime createdAt; 
    
    private Integer progressRate;
    private String managerName;
}