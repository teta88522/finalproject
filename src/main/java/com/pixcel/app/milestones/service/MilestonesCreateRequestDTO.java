package com.pixcel.app.milestones.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 마일스톤 생성 시 화면에서 넘어오는 데이터를 담는것
 * **/

@Getter
@Setter
@ToString
public class MilestonesCreateRequestDTO {
	private String milestoneId;

	@NotBlank(message = "마일스톤 제목은 필수입니다") //null이 아니고 공백 문자열("")이 아닌지 검사
	private String title; //마일스톤 명칭
	
	@NotNull(message = "담당자를 지정해야 합니다.") //null 값만 아니면 됩니다. (숫자 등 객체 타입 전용) 
    private String managerId; // 담당자 ID
	
	@NotBlank(message = "상태를 선택해주세요")
	private String statusCode; //
	
	@NotNull(message = "시작일을 입력해주세요")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //json으로 데이터를 보낼떄
	@DateTimeFormat(pattern = "yyyy-MM-dd") //일반 데이터 타입 포멧 양식
	private LocalDate startDate;
	
	@NotNull(message = "종료일을 입력해주세요")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate; //종료일 데이터 받는것
	
	private String description; //설명
	
//	@NotNull(message = "버전 정보를 선택해주세요")
	private String versionId; //버전선택
	
	private String selectedIssueIds; //화면에서 선택한 일감 번호들의 리스트를 JSON 배열 형태로 받습니다.
	
    
    private Integer progressRate;
    private String managerName;
}
