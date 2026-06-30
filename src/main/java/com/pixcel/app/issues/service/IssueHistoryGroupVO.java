package com.pixcel.app.issues.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueHistoryGroupVO {

	private String historyGroupId;
	private String changedBy;
	private String changedByName;
	private String changeTypeCode;
	private String changeTypeDisplayName;
	private String reason;
	private Date changedAt;
	private List<IssuesVO> historyList = new ArrayList<>();
}
