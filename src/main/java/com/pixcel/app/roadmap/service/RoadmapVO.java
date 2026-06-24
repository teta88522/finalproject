package com.pixcel.app.roadmap.service;

import java.time.LocalDateTime;
import java.util.List;

import com.pixcel.app.milestones.service.MilestonesVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapVO {
	private String versionId;
	private String projectId;
	private String versionName;
	private String description;
	private String statusCode;
	private String defaultYn;
	private LocalDateTime createdAt;
	
	List<MilestonesVO> milestoneList;
	private int progressRate;
}
