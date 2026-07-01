package com.pixcel.app.bug.service;

import java.util.List;

public interface BugService {
	
	public List<BugVO> selectBugList(BugSearchVO bugSearchVO);
	
	public BugVO selectBugDetail(String bugId);
	
	public int insertBug(BugVO bugVO);
	
	public int updateBug(BugVO bugVO);
	
    public int updateBugByReporter(BugVO bugVO);
	
	public int delete(String bugId);
	
	public int deleteBugByReporter(BugVO bugVO);
	
	public BugVO selectBugAddInfoByExecutionId(String executionId);
}
