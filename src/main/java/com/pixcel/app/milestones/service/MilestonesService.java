package com.pixcel.app.milestones.service;

import java.util.List;

public interface MilestonesService {
 public String createMilestone(MilestonesCreateRequestDTO requestDTO); //마일스톤 생성 인터페이스
 
 public List<MilestonesMemberDTO> getManagerList(String teamId);
 public List<MilestonesIssueDTO> getIssueList(String keyword, String versionId);
 
 public MilestonesCreateRequestDTO getMilestoneDetail(String milestoneId); //마일스톤 상세조회
 
 public int updateMilestone(MilestonesCreateRequestDTO updateVO); //마일스톤 업데이트
 
 public int deleteMilestone(MilestonesVO milestonesVO);	  //마일스톤 삭제
 
 public List<MilestoneListResponseDTO> getMilestoneList(MilestoneSearchVO searchVO);
}