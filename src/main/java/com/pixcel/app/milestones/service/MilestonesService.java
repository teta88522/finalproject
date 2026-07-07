package com.pixcel.app.milestones.service;

import java.util.List;

import com.pixcel.app.issues.service.IssuesVO;

public interface MilestonesService {
    // -- 권한 처리
    public boolean checkProjectPermission(String projectId, String userId, String permissionCode);
    // -- 권한 처리 끝

    public String createMilestone(MilestonesVO milestoneVO, String userId); //마일스톤 생성 인터페이스
    
    public List<MilestonesMemberDTO> getManagerList(String teamId);
    public List<IssuesVO> getIssueList(String keyword, String versionId, String projectId);
    
    public MilestonesVO getMilestoneDetail(String milestoneId, String projectId); //마일스톤 상세조회
    
    public int updateMilestone(MilestonesVO updateVO, String userId); //마일스톤 업데이트
    
    public int deleteMilestone(String mileStoneId, String projectId, String userId);	  //마일스톤 삭제
 
 public List<MilestonesVO> getMilestoneList(String projectId);
 
 public List<IssuesVO> selectConnectedIssues(String milestoneId);

 public List<IssuesVO> selectMilestoneHistoryList(String milestoneId);
}