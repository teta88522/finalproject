package com.pixcel.app.milestones.service;

import java.util.List;

public interface MilestonesService {
 String createMilestone(MilestonesCreateRequestDTO requestDTO); //마일스톤 생성 인터페이스
 
 List<MilestonesMemberDTO> getManagerList(String teamId);
 List<MilestonesIssueDTO> getIssueList(String keyword, String versionId);
 
 MilestonesVO getMilestoneDetail(String milestoneId); //마일스톤 상세조회
}
