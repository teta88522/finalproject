package com.pixcel.app.manage.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.manage.mapper.ManageMapper;
import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;
import com.pixcel.app.manage.service.ManageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManageServiceImpl implements ManageService {
	
	private final ManageMapper manageMapper;
	
	@Override
	public List<ManageProjectVO> selectMyManageProjectList(String ownerId) {
		return manageMapper.selectMyManageProjectList(ownerId);
	}

	@Override
	public List<ManageGroupVO> selectMyManageGroupList(String ownerId) {
		return manageMapper.selectMyManageGroupList(ownerId);
	}

}
