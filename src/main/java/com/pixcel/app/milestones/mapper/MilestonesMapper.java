package com.pixcel.app.milestones.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.milestones.service.MilestonesVO;

@Mapper
public interface MilestonesMapper {


	public List<MilestonesVO> selectAll();					  //전체조회
	public MilestonesVO selectMilestone(MilestonesVO milestoneVO); //상세조회
    public int insertMilestone(MilestonesVO milestoneVO);	  //마일스톤 생성
    public int updateMilestone(MilestonesVO milestoneVO);	  //마일스톤 수정
    public int deleteMilestone(MilestonesVO milestonesVO);	  //마일스톤 삭제
	
	
}
