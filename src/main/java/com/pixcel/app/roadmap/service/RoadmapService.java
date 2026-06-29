package com.pixcel.app.roadmap.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.milestones.service.MilestonesVO;

@Service
public interface RoadmapService {
	public String insertRoadmap(RoadmapVO roadmap);
	public List<RoadmapVO> getSettingList(RoadmapVO roadmapVO);
	public int updateRoadmap(RoadmapVO roadmapVO);
	public RoadmapVO getsettingDetail(String versionId, String projectId);
	public int deleteRoadmap(String versionId, String projectId);
	public List<RoadmapVO> getRoadmapFull(String projectId);
	public RoadmapVO getRoadmapDetail(String projectId, String versionId);
	public List<MilestonesVO> getRoadmapMilestones(String projectId, String versionId);
}
