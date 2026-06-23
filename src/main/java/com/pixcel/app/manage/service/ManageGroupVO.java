package com.pixcel.app.manage.service;

import lombok.Data;

@Data
public class ManageGroupVO {
	
	private String projectGroupId;
	private String projectId;
	private String roleId;
	
	private String groupName;
	private String groupAnswer;
	
	private String projectName;
	private String roleName;
	private int memberCount;
	
}
