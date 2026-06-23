package com.pixcel.app.roadmap.service.impl;

import org.springframework.stereotype.Service;

import com.pixcel.app.roadmap.mapper.RoadmapMapper;
import com.pixcel.app.roadmap.service.RoadmapService;
import com.pixcel.app.roadmap.service.RoadmapVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoadmapServicelmpl implements RoadmapService {
	
	private final RoadmapMapper roadmapMapper;
	
	@Override
	@Transactional
	public String insertVersion(RoadmapVO roadmapVO) {
		roadmapMapper.insertVersion(roadmapVO);
		return roadmapVO.getVersionId();
	}
}
