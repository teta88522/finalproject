package com.pixcel.app.milestones.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.milestones.service.MilestonesIssueDTO;
import com.pixcel.app.milestones.service.MilestonesMemberDTO;
import com.pixcel.app.milestones.service.MilestonesCreateRequestDTO;
import com.pixcel.app.milestones.service.MilestonesVO;

@Mapper
public interface MilestonesMapper {

	// 팀 ID를 조건으로 해당 팀의 관리자급 멤버들을 조회합니다.
	public List<MilestonesMemberDTO> getManagerList(String teamId);
	// @Param은 MyBatis XML 파일까지 안전하게 전달해 주는 이름표(라벨)
	// keyword는 일감 번호(ID)나 제목(Title)으로 검색하기 위해 필요
	// version은 특정 버전 내의 일감만 조회해야 하는 **'필터링 조건'**을 처리하기 위해 반드시 필요
	public List<MilestonesIssueDTO> getIssueList(@Param("keyword") String keyword, @Param("versionId") String versionId);
	
	public void insertMilestone(MilestonesCreateRequestDTO dto);
    //저장 작업은 데이터를 조회해서 가져오는 게 아니라 "DB에 데이터를 넣는 동작" 자체로 끝나기 때문입니다.
	//프로시저 사용 : 프로시저 내부에서는 마일스톤을 하나 INSERT하고, 이어서 여러 개의 일감을 UPDATE하는 복잡한 로직이기 떄문에
	
	MilestonesVO getMilestoneDetail(String milestoneId); //마일스톤 상세페이지
	
    public int deleteMilestone(MilestonesVO milestonesVO);	  //마일스톤 삭제
	
    
    
}
