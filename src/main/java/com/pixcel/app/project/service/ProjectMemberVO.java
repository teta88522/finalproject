package com.pixcel.app.project.service;

import lombok.Data;

@Data
public class ProjectMemberVO {
	private String projectMemberId;
	private String projectId;
	private String teamMemberId;
	private String projectGroupId;
	private String roleId;
	private String joinedAt;
	
	private String projectName;
	private String teamId;
	private String teamName;
	private String loginId;
	private String userId;
	private String userName;
	private String email;
	private String phone;
	
	private String groupName;
	private String roleName;
	
	private String joinedYn;
}
