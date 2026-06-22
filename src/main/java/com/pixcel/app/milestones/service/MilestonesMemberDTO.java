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
public class MilestonesMemberDTO {
	private String teamId;       
	private String userId;    
	private String userName;
	private String teamRoleCode;
}
