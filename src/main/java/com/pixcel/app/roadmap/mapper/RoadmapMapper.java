package com.pixcel.app.roadmap.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.roadmap.service.RoadmapVO;

@Mapper
public interface RoadmapMapper {
	public void insertVersion(RoadmapVO roadmap);
}
