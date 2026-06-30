package com.pixcel.app.gantt.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pixcel.app.gantt.service.GanttResponse;
import com.pixcel.app.gantt.service.GanttService;
import com.pixcel.app.gantt.service.GanttTaskDTO;
import com.pixcel.app.issues.service.IssuesVO;
import com.pixcel.app.milestones.mapper.MilestonesMapper;
import com.pixcel.app.milestones.service.MilestonesVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GanttServiceImpl implements GanttService{
	
	private final MilestonesMapper milestonesMapper;
	
	@Override
	public GanttResponse getGanttChartData(String projectId) {
		List<GanttTaskDTO>ganttDataList = new ArrayList<>();
		List<MilestonesVO>milestoneList = milestonesMapper.getMilestoneList(projectId);
		
		for(MilestonesVO mVO : milestoneList) {
			GanttTaskDTO mTask = new GanttTaskDTO();
			mTask.setId("M_" + mVO.getMilestoneId());
			mTask.setText(mVO.getTitle());
			mTask.setStart_date(mVO.getStartDate());
			mTask.setEnd_date(mVO.getEndDate());
			mTask.setType("project");
			mTask.setParent(null);
			mTask.setProgress(mVO.getTotalProgressRate() != null ? mVO.getTotalProgressRate() / 100.0 : 0.0);
			
			if (mVO.getStartDate() != null && mVO.getEndDate() != null) {
			    mTask.setDuration((int) ChronoUnit.DAYS.between(mVO.getStartDate(), mVO.getEndDate()) + 1);
			} else {
			    mTask.setDuration(1);
			}
			ganttDataList.add(mTask);
			
			
		if(mVO.getIssueList() != null) {
			for(IssuesVO iVO : mVO.getIssueList()) {
				if(iVO.getIssueId() == null) continue;
				
				GanttTaskDTO iTask = new GanttTaskDTO();
				iTask.setId("I_" + iVO.getIssueId());
				iTask.setText(iVO.getTitle());
				iTask.setType("task");
				iTask.setParent("M_" + mVO.getMilestoneId());
				iTask.setProgress(iVO.getProgressRate() != null ? iVO.getProgressRate() / 100.0 : 0.0);
				iTask.setStart_date(mVO.getStartDate());
				iTask.setEnd_date(iVO.getDueDate());
				iTask.setAssigneeName(iVO.getAssigneeName());
				iTask.setIssueStatusName(iVO.getIssueStatusName());
				
				if(mVO.getStartDate() != null && iVO.getDueDate() != null) {
					long duration = ChronoUnit.DAYS.between(mVO.getStartDate(), iVO.getDueDate()) + 1;
					iTask.setDuration(duration > 0 ? (int) duration : 1); // 기간이 마이너스가 나오는 상황 방지
				} else {
					iTask.setDuration(1);
				}
				ganttDataList.add(iTask);
			}
		}
			
		}
		return new GanttResponse(ganttDataList, Collections.emptyList());
	}
	
}
