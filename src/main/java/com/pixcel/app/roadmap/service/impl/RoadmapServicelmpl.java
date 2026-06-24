package com.pixcel.app.roadmap.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.milestones.service.MilestonesIssueDTO;
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
	public List<RoadmapVO> getRoadmapList(RoadmapVO roadmapVO){
		return roadmapMapper.getRoadmapList(roadmapVO);
	}
	@Override
	public int updateRoadmap(RoadmapVO roadmapVO) {
	    return roadmapMapper.updateRoadmap(roadmapVO);
	}
	@Override
	public RoadmapVO getRoadmapDetail(String versionId, String projectId){
		return roadmapMapper.getRoadmapDetail(versionId, projectId);
	}
	@Override
	public int deleteRoadmap(String versionId, String projectId){
		return roadmapMapper.deleteRoadmap(versionId, projectId);
	}
	@Override
    public List<RoadmapVO> getRoadmapFull(String projectId) {
        
        // 1. MyBatis Mapper를 통해 3단 계층 데이터 통째로 조회
        List<RoadmapVO> roadmapList = roadmapMapper.getRoadmapFull(projectId);

        // 2. 진행률(%) 계산 로직 (방어 코드 포함)
        if (roadmapList != null && !roadmapList.isEmpty()) {
            for (RoadmapVO roadmap : roadmapList) {
                int roadmapTotalIssues = 0;       // 로드맵 전체 일감 수
                int roadmapCompletedIssues = 0;   // 로드맵 전체 완료 일감 수

                if (roadmap.getMilestoneList() != null && !roadmap.getMilestoneList().isEmpty()) {
                    for (MilestonesVO milestone : roadmap.getMilestoneList()) {
                        int milestoneTotalIssues = 0;       // 마일스톤 개별 일감 수
                        int milestoneCompletedIssues = 0;   // 마일스톤 개별 완료 일감 수

                        if (milestone.getIssueList() != null && !milestone.getIssueList().isEmpty()) {
                            for (MilestonesIssueDTO issue : milestone.getIssueList()) {
                                
                                // LEFT JOIN으로 인해 일감이 없는 빈 객체가 들어올 수 있으므로 ID 존재 여부 체크
                                if (issue.getIssueId() != null) {
                                    // 개수 누적
                                    milestoneTotalIssues++;
                                    roadmapTotalIssues++;

                                    // 완료 상태(a003) 체크
                                    if ("a003".equals(issue.getIssueStatusId())) {
                                        milestoneCompletedIssues++;
                                        roadmapCompletedIssues++;
                                    }
                                }
                            }
                        }

                        // 3-1. 마일스톤별 개별 진행률 계산 및 세팅
                        int milestoneProgress = 0;
                        if (milestoneTotalIssues > 0) {
                            milestoneProgress = (int) Math.round(((double) milestoneCompletedIssues / milestoneTotalIssues) * 100);
                        }
                        milestone.setTotalProgressRate(milestoneProgress);
                    }
                }

                // 3-2. 로드맵 전체 진행률 계산 및 세팅
                int roadmapProgress = 0;
                if (roadmapTotalIssues > 0) {
                    roadmapProgress = (int) Math.round(((double) roadmapCompletedIssues / roadmapTotalIssues) * 100);
                }
                roadmap.setProgressRate(roadmapProgress); // RoadmapVO에 만들어둔 progressRate 필드에 저장
            }
        }

        // 완성된 리스트 반환
        return roadmapList;
    }
}
