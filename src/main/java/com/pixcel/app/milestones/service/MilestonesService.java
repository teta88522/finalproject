package com.pixcel.app.milestones.service;

import java.util.List;

public interface MilestonesService {
 public String createMilestone(MilestonesVO milestoneVO); //마일스톤 생성 인터페이스
 
 public List<MilestonesMemberDTO> getManagerList(String teamId);
 public List<MilestonesIssueDTO> getIssueList(String keyword, String versionId);
 
 public MilestonesVO getMilestoneDetail(String milestoneId, String projectId); //마일스톤 상세조회
 
 public int updateMilestone(MilestonesVO updateVO); //마일스톤 업데이트
 
 public int deleteMilestone(String mileStoneId,String projectId);	  //마일스톤 삭제
 
 public List<MilestonesVO> getMilestoneList(MilestoneSearchVO searchVO);
 
 public List<MilestonesIssueDTO> getConnectedIssues(String milestoneId);
}