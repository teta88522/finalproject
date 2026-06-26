package com.pixcel.app.roadmap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.roadmap.mapper.RoadmapMapper;
import com.pixcel.app.roadmap.service.RoadmapService;
import com.pixcel.app.roadmap.service.RoadmapVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapServicelmpl implements RoadmapService {
	
	private final RoadmapMapper roadmapMapper;
	
	@Override
	public String insertRoadmap(RoadmapVO roadmapVO) {
		roadmapMapper.insertVersion(roadmapVO);
		return roadmapVO.getVersionId();
	}
	
	@Override
	public List<RoadmapVO> getSettingList(RoadmapVO roadmapVO){
		return roadmapMapper.getRoadmapList(roadmapVO);
	}
	@Override
	public int updateRoadmap(RoadmapVO roadmapVO) {
	    return roadmapMapper.updateRoadmap(roadmapVO);
	}
	@Override
	public RoadmapVO getsettingDetail(String versionId, String projectId){
		return roadmapMapper.getsettingDetail(versionId, projectId);
	}
	@Override
	public int deleteRoadmap(String versionId, String projectId){
		return roadmapMapper.deleteRoadmap(versionId, projectId);
	}
	@Override
    public List<RoadmapVO> getRoadmapFull(String projectId) {
        List<RoadmapVO> roadmapList = roadmapMapper.getRoadmapFull(projectId);
        return roadmapList;
    }
	@Override
	public RoadmapVO getRoadmapDetail(String versionId, String projectId){
	        return roadmapMapper.getRoadmapDetail(projectId, versionId);
	};
}
