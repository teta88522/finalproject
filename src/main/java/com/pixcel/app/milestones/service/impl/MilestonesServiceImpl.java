package com.pixcel.app.milestones.service.impl;

import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO; // DTO 임포트 확인
import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.milestones.mapper.MilestonesMapper;
import com.pixcel.app.roadmap.mapper.RoadmapMapper;
import com.pixcel.app.roadmap.service.RoadmapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestonesServiceImpl implements MilestonesService {

    private final MilestonesMapper milestonesMapper;
    private final RoadmapMapper roadmapMapper;
    // -- 권한 처리
    private final com.pixcel.app.issues.mapper.IssuesMapper issuesMapper;
    // -- 권한 처리 끝

    // 1. 마일스톤 생성 로직 (트랜잭션 적용)
  
    // -- 권한 처리
    @Override
    public boolean checkProjectPermission(String projectId, String userId, String permissionCode) {
        if (userId == null || projectId == null) {
            return false;
        }
        return issuesMapper.countProjectPermission(projectId, userId, permissionCode, null) > 0;
    }
    // -- 권한 처리 끝

    // 1. 마일스톤 생성 로직 (트랜잭션 적용)
  
    @Override
    @Transactional // 마일스톤 생성과 일감 업데이트가 한 묶음으로 처리되도록 트랜잭션 걸기
    public String createMilestone(MilestonesVO milestoneVO, String userId) {
        // -- 권한 처리
        if (!checkProjectPermission(milestoneVO.getProjectId(), userId, "p002")) {
            throw new SecurityException("마일스톤 생성 권한이 없습니다.");
        }
        // -- 권한 처리 끝
        
        // [설명]
        // 예전에는 여기서 requestDTO.getIssueIds()를 꺼내서 String.join(",", ...) 처리를 했어야 했습니다.
        // 하지만! 작성해주신 JS의 validateForm() 함수에서 이미 "1234,1235" 형태로 만들어서
        // requestDTO.getSelectedIssueIds()에 담아 보냈으므로, 여기선 아무런 가공을 할 필요가 없습니다!
        
        // 바로 Mapper(DB 공장)로 쿨하게 던져줍니다.
        milestonesMapper.insertMilestone(milestoneVO);
        
        return milestoneVO.getMilestoneId();
    }

    // 2. 담당자 목록 조회
    @Override
    public List<MilestonesMemberDTO> getManagerList(String teamId) {
        return milestonesMapper.getManagerList(teamId);
    }

    // 3. 일감 목록 조회
    @Override
    public List<IssuesVO> getIssueList(String keyword, String versionId, String projectId) {
        return milestonesMapper.getIssueList(keyword, versionId, projectId);
    }
    // 4. 상세 조회
    @Override
    public MilestonesVO getMilestoneDetail(String milestoneId,String projectId) {
        // Mapper를 통해 DB에서 데이터를 꺼내옵니다.
        return milestonesMapper.getMilestoneDetail(milestoneId);
    }
    // 5. 수정
    @Override
    @Transactional
    public int updateMilestone(MilestonesVO updateVO, String userId) {
        // -- 권한 처리
        if (!checkProjectPermission(updateVO.getProjectId(), userId, "p003")) {
            throw new SecurityException("마일스톤 수정 권한이 없습니다.");
        }
        // -- 권한 처리 끝

        // [안전장치] 원래 마일스톤이 연결되어 있던 기존 로드맵이 완료(k003) 상태인지 먼저 체크
        MilestonesVO currentMilestone = milestonesMapper.getMilestoneDetail(updateVO.getMilestoneId());
        if (currentMilestone != null && currentMilestone.getVersionId() != null) {
            RoadmapVO currentRoadmap = roadmapMapper.getsettingDetail(currentMilestone.getVersionId(), updateVO.getProjectId());
            if (currentRoadmap != null && "k003".equals(currentRoadmap.getStatusCode())) {
                throw new IllegalArgumentException("이미 완료된 로드맵에 속한 마일스톤은 수정할 수 없습니다.");
            }
        }

        // [안전장치] 새로 변경하려는 타겟 로드맵이 완료(k003) 상태인지 체크
        if (updateVO.getVersionId() != null) {
            RoadmapVO targetRoadmap = roadmapMapper.getsettingDetail(updateVO.getVersionId(), updateVO.getProjectId());
            if (targetRoadmap != null && "k003".equals(targetRoadmap.getStatusCode())) {
                throw new IllegalArgumentException("완료된 로드맵으로는 마일스톤을 연결할 수 없습니다.");
            }
        }

        // [안전장치] 연결된 일감이 없거나 모든 일감의 평균 진행률이 100%가 아닌 경우 완료(L003) 상태로 변경할 수 없게 차단
        if ("L003".equals(updateVO.getStatusCode())) {
            List<IssuesVO> connectedIssues = milestonesMapper.selectConnectedIssues(updateVO.getMilestoneId());
            if (connectedIssues == null || connectedIssues.isEmpty()) {
                throw new IllegalArgumentException("연결된 일감이 없는 마일스톤은 완료할 수 없습니다.");
            }
            double avg = connectedIssues.stream()
                    .mapToInt(issue -> issue.getProgressRate() != null ? issue.getProgressRate() : 0)
                    .average()
                    .orElse(0.0);
            if (Math.round(avg) < 100) {
                throw new IllegalArgumentException("모든 일감의 평균 진행률이 100%여야 완료할 수 있습니다.");
            }
        }
        return milestonesMapper.updateMilestone(updateVO);
    }
    
    // 6. 삭제
    @Override
    @Transactional
    public int deleteMilestone(String milestoneId, String projectId, String userId) {
        // -- 권한 처리
        if (!checkProjectPermission(projectId, userId, "p004")) {
            throw new SecurityException("마일스톤 삭제 권한이 없습니다.");
        }
        // -- 권한 처리 끝
    	return milestonesMapper.deleteMilestone(milestoneId);
    }
    
    //7. 목록조회
    @Override
    public  List<MilestonesVO> getMilestoneList(String projectId){
    	return milestonesMapper.getMilestoneList(projectId);
    };
    
    //8. 등록된 일감목록
    @Override
    public List<IssuesVO> selectConnectedIssues(String milestoneId) {
        return milestonesMapper.selectConnectedIssues(milestoneId);
    }

    @Override
    public List<IssuesVO> selectMilestoneHistoryList(String milestoneId){
        return milestonesMapper.selectMilestoneHistoryList(milestoneId);
    }
}