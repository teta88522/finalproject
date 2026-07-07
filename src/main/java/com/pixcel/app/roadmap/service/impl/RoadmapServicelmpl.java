package com.pixcel.app.roadmap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.milestones.service.MilestonesVO;
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
		
		RoadmapVO current = roadmapMapper.getsettingDetail(roadmapVO.getVersionId(), roadmapVO.getProjectId());
		
		if ("k003".equals(current.getStatusCode())) {
	        throw new RuntimeException("완료된 로드맵은 수정할 수 없습니다.");
	    }
		
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
	public RoadmapVO getRoadmapDetail(String projectId, String versionId){
	        return roadmapMapper.getRoadmapDetail(versionId, projectId);
	};
	@Override
    public List<MilestonesVO> getRoadmapMilestones(String projectId, String versionId) {
        // 매퍼의 2번 쿼리 호출
        return roadmapMapper.getRoadmapMilestones(projectId, versionId);
    }

	@Override
	public List<RoadmapVO> getVersionId(String projectId) {
		return roadmapMapper.getVersionId(projectId);
	}
	
	@Override
	@Transactional
	public int updateCompletion(String versionId, String projectId) {
		// [안전장치] 연결된 일감이 없거나 모든 일감의 평균 진행률이 100%가 아닌 경우 로드맵 완료(k003)를 할 수 없게 차단
		List<MilestonesVO> milestoneList = roadmapMapper.getRoadmapMilestones(projectId, versionId);
		
		int totalIssues = 0;
		int totalProgressSum = 0;
		
		if (milestoneList != null) {
			for (MilestonesVO milestone : milestoneList) {
				// [안전장치] 완료되지 않은 마일스톤이 존재한다면 로드맵 완료를 차단
				if (milestone.getMilestoneId() != null && !milestone.getMilestoneId().startsWith("UNASSIGNED_")) {
					if (!"L003".equals(milestone.getStatusCode())) {
						throw new IllegalArgumentException("완료되지 않은 마일스톤이 존재하여 로드맵을 완료할 수 없습니다. (미완료 마일스톤: " + milestone.getTitle() + ")");
					}
				}
				
				if (milestone.getIssueList() != null) {
					for (IssuesVO issue : milestone.getIssueList()) {
						if (issue.getIssueId() != null) { // 빈 객체 방지
							totalIssues++;
							totalProgressSum += (issue.getProgressRate() != null ? issue.getProgressRate() : 0);
						}
					}
				}
			}
		}
		
		if (totalIssues == 0) {
			throw new IllegalArgumentException("연결된 일감이 없는 로드맵은 완료할 수 없습니다.");
		}
		
		int avgProgress = Math.round((float) totalProgressSum / totalIssues);
		if (avgProgress < 100) {
			throw new IllegalArgumentException("모든 일감의 평균 진행률이 100%여야 로드맵을 완료할 수 있습니다. (현재 평균 진행률: " + avgProgress + "%)");
		}
		
		return roadmapMapper.updateCompletion(versionId, projectId);
	}
}
