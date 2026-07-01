package com.pixcel.app.bug.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.bug.mapper.BugMapper;
import com.pixcel.app.bug.service.BugSearchVO;
import com.pixcel.app.bug.service.BugService;
import com.pixcel.app.bug.service.BugVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {
	
	private final BugMapper bugMapper;
	
	@Override
	public List<BugVO> selectBugList(BugSearchVO bugSearchVO) {
		return bugMapper.selectBugList(bugSearchVO);
	}

	@Override
	public BugVO selectBugDetail(String bugId) {
		return bugMapper.selectBugDetail(bugId);
	}

	@Override
	public int insertBug(BugVO bugVO) {
		 return bugMapper.insertBug(bugVO);
	}

	@Override
	public int updateBug(BugVO bugVO) {
		 return bugMapper.updateBug(bugVO);
	}

	@Override
	public int delete(String bugId) {
		return bugMapper.deleteBug(bugId);
	}

	@Override
	public int deleteBugByReporter(BugVO bugVO) {
		return bugMapper.deleteBugByReporter(bugVO);
	}

	@Override
	public int updateBugByReporter(BugVO bugVO) {
		 return bugMapper.updateBugByReporter(bugVO);
	}

	@Override
	public BugVO selectBugAddInfoByExecutionId(String executionId) {
		return bugMapper.selectBugAddInfoByExecutionId(executionId);
	}

}
