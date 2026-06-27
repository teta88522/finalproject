package com.pixcel.app.roadmap.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.roadmap.service.RoadmapVO;

@Mapper
public interface RoadmapMapper {
	public void insertVersion(RoadmapVO roadmap);
	public List<RoadmapVO> getRoadmapList(RoadmapVO roadmapVO);
	public int updateRoadmap(RoadmapVO roadmapVO);
	public RoadmapVO getsettingDetail(@Param("versionId") String versionId, @Param("projectId") String projectId);
	public int deleteRoadmap(@Param("versionId") String versionId, @Param("projectId") String projectId);
	public List<RoadmapVO> getRoadmapFull(String projectId);
	public RoadmapVO getRoadmapDetail(@Param("versionId") String versionId, @Param("projectId") String projectId);
	public List<MilestonesVO> getRoadmapMilestones(@Param("projectId") String projectId, @Param("versionId") String versionId);
}
