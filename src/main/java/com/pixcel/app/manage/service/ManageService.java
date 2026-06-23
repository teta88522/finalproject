package com.pixcel.app.manage.service;

import java.util.List;

public interface ManageService {
	//관리자가 생성한 프로젝트 목록 조회
	public List<ManageProjectVO> selectMyManageProjectList(String ownerId);
	
	//관리자가 생성한 프로젝트의 그룹 목록
	public List<ManageGroupVO> selectMyManageGroupList(String ownerId);
}
