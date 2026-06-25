package com.pixcel.app.milestones.service;

import java.util.List;

import com.pixcel.app.issues.service.IssuesVO;

public interface MilestonesService {
 public String createMilestone(MilestonesVO milestoneVO); //마일스톤 생성 인터페이스
 
 public List<MilestonesMemberDTO> getManagerList(String teamId);
 public List<IssuesVO> getIssueList(String keyword, String versionId, String projectId);
 
 public MilestonesVO getMilestoneDetail(String milestoneId, String projectId); //마일스톤 상세조회
 
 public int updateMilestone(MilestonesVO updateVO); //마일스톤 업데이트
 
 public int deleteMilestone(String mileStoneId,String projectId);	  //마일스톤 삭제
 
 public List<MilestonesVO> getMilestoneList(MilestoneSearchVO searchVO);
 
 public List<IssuesVO> selectConnectedIssues(String milestoneId);
}