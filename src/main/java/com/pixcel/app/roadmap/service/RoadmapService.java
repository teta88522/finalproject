package com.pixcel.app.roadmap.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface RoadmapService {
	public String insertRoadmap(RoadmapVO roadmap);
	public List<RoadmapVO> getRoadmapList(RoadmapVO roadmapVO);
	public int updateRoadmap(RoadmapVO roadmapVO);
	public RoadmapVO getRoadmapDetail(String versionId, String projectId);
	public int deleteRoadmap(String versionId, String projectId);
	List<RoadmapVO> getRoadmapFull(String projectId);
}
