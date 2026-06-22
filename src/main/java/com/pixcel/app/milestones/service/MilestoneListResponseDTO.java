package com.pixcel.app.milestones.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MilestoneListResponseDTO {
    
    // --- 1. 마일스톤 자체의 기본 정보 ---
    private String milestoneId;
    private String title;           // 마일스톤 제목
    private String managerName;     // 담당자명
    private String statusCode;      // 마일스톤 상태
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;    // 기간 (시작일)
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;      // 기간 (종료일)
    
    private Integer totalProgressRate; //마일스톤 진행률   
    private List<MilestonesIssueDTO> issueList; //하위리스트 일감 표시
}
