package com.pixcel.app.milestones.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestonesIssueDTO {
	private String milestoneId;
    private String issueId;       
    private String title;         
    private String versionId;     
    private int progressRate;       
    private String managerId;    
    private String managerName;
}