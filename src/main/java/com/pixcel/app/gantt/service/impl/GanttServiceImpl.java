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
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.roadmap.mapper.RoadmapMapper;
import com.pixcel.app.roadmap.service.RoadmapVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GanttServiceImpl implements GanttService {
	
	private final RoadmapMapper roadmapMapper;
	
	@Override
	public GanttResponse getGanttChartData(String projectId) {
		List<GanttTaskDTO> ganttDataList = new ArrayList<>();
		
		RoadmapVO roadmapSearch = new RoadmapVO();
		roadmapSearch.setProjectId(projectId);
		List<RoadmapVO> roadmapList = roadmapMapper.getRoadmapList(roadmapSearch);
		
		for (RoadmapVO rVO : roadmapList) {
			List<MilestonesVO> milestoneList = roadmapMapper.getRoadmapMilestones(projectId, rVO.getVersionId());
			
			for (MilestonesVO mVO : milestoneList) {
				GanttTaskDTO mTask = new GanttTaskDTO();
				mTask.setId("M_" + mVO.getMilestoneId());
				
				if (mVO.getMilestoneId().startsWith("UNASSIGNED")) {
					mTask.setText("미지정 일감 (" + rVO.getVersionName() + ")");
				} else {
					mTask.setText(mVO.getTitle());
				}
				
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
				
				if (mVO.getIssueList() != null) {
					for (IssuesVO iVO : mVO.getIssueList()) {
						if (iVO.getIssueId() == null) continue;
						
						GanttTaskDTO iTask = new GanttTaskDTO();
						iTask.setId("I_" + iVO.getIssueId());
						iTask.setText(iVO.getTitle());
						iTask.setType("task");
						iTask.setParent("M_" + mVO.getMilestoneId());
						iTask.setProgress(iVO.getProgressRate() != null ? iVO.getProgressRate() / 100.0 : 0.0);
						
						java.time.LocalDate issueStart = iVO.getStartDate() != null ? iVO.getStartDate() : mVO.getStartDate();
						iTask.setStart_date(issueStart);
						iTask.setEnd_date(iVO.getDueDate());
						iTask.setAssigneeName(iVO.getAssigneeName());
						iTask.setIssueStatusName(iVO.getIssueStatusName());
						
						if (issueStart != null && iVO.getDueDate() != null) {
							long duration = ChronoUnit.DAYS.between(issueStart, iVO.getDueDate()) + 1;
							iTask.setDuration(duration > 0 ? (int) duration : 1);
						} else {
							iTask.setDuration(1);
						}
						ganttDataList.add(iTask);
					}
				}
			}
		}
		return new GanttResponse(ganttDataList, Collections.emptyList());
	}
}

