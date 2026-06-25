package com.pixcel.app.milestones.service.impl;

import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.milestones.service.MilestoneSearchVO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO; // DTO 임포트 확인
import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.milestones.mapper.MilestonesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestonesServiceImpl implements MilestonesService {

    private final MilestonesMapper milestonesMapper;

    // 1. 마일스톤 생성 로직 (트랜잭션 적용)
  
    @Override
    @Transactional // 마일스톤 생성과 일감 업데이트가 한 묶음으로 처리되도록 트랜잭션 걸기
    public String createMilestone(MilestonesVO milestoneVO) {
        
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
    public int updateMilestone(MilestonesVO updateVO) {
        return milestonesMapper.updateMilestone(updateVO);
    }
    
    // 6. 삭제
    @Override
    @Transactional
    public int deleteMilestone(String milestoneId,String projectId) {
    	return milestonesMapper.deleteMilestone(milestoneId);
    }
    
    //7. 목록조회
    @Override
    public  List<MilestonesVO> getMilestoneList(MilestoneSearchVO searchVO){
    	return milestonesMapper.getMilestoneList(searchVO);
    };
    
    //8. 등록된 일감목록
    @Override
    public List<IssuesVO> selectConnectedIssues(String milestoneId) {
        return milestonesMapper.selectConnectedIssues(milestoneId);
    }
}