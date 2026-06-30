package com.pixcel.app.bug.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.bug.service.BugSearchVO;
import com.pixcel.app.bug.service.BugVO;

@Mapper
public interface BugMapper {
	//버그 목록
	public List<BugVO> selectBugList(BugSearchVO bugSearchVO);
	
	// 버그 상세
	public BugVO selectBugDetail(String bugId);
	
	// 버그 등록
	public int insertBug(BugVO bugVO);
	
	// 버그 수정
	public int updateBug(BugVO bugVO);
	
	// 버그 삭제
	public int deleteBug(String bugId);
	
	// 등록자 삭제
	public int deleteBugByReporter(BugVO bugVO);
	
}
