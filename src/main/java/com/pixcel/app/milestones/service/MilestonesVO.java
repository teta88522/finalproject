package com.pixcel.app.milestones.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotBlank(message = "마일스톤 제목은 필수입니다")
    private String title;        
    private String description; 
    @NotNull(message = "담당자를 지정해야 합니다.")
    private String managerId; 
    @NotBlank(message = "상태를 선택해주세요")
    private String statusCode;   
    private String statusName;
    @NotNull(message = "시작일을 입력해주세요")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //json으로 데이터를 보낼떄
	@DateTimeFormat(pattern = "yyyy-MM-dd") //일반 데이터 타입 포멧 양식
    private LocalDate startDate;
    @NotNull(message = "종료일을 입력해주세요")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;  
    private LocalDateTime createdAt; 
    private String projectId;
    
    private String managerName;
    private String selectedIssueIds;
    
    private Integer totalProgressRate;          // 마일스톤 전체 진행률
    private List<MilestonesIssueDTO> issueList; // 하위 일감 리스트
}
