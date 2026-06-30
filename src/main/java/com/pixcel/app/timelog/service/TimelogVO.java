package com.pixcel.app.timelog.service;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TimelogVO {

	private String timeLogId;
	private String issueId;
	private String originalIssueId;
	private String userId;
	private String settingCodeId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate workDate;

	private Integer hours;
	private String description;
	private String descriptionPreview;
	private Date createdAt;

	private String projectId;
	private String projectName;
	private String ownerId;

	private Integer issueNo;
	private String displayIssueNo;
	private String issueTitle;
	private String issueStatusName;
	private String closedYn;
	private String assigneeId;
	private String assigneeName;

	private String userName;
	private String workTypeName;

	private String optionGroup;
	private Integer optionSort;
	private Integer rowSort;
	private Integer rowNo;

	private String updatePermissionYn;
	private Integer issueAccessCount;
	private Integer workTypeCount;
	private Integer totalHours;
	private Integer totalCount;

	private String issueSearchKeyword;
}
