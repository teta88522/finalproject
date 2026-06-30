package com.pixcel.app.timelog.service;

import java.util.Map;

public interface TimelogService {

	Map<String, Object> getCreatePageData(String projectId, String userId, String issueSearchKeyword,
			String selectedIssueId);

	void createTimelog(TimelogVO timelog, String userId);

	Map<String, Object> getDetailPageData(String projectId, String issueId, String timeLogId, String userId);

	Map<String, Object> getUpdatePageData(String projectId, String issueId, String timeLogId, String userId,
			String issueSearchKeyword, String selectedIssueId);

	void updateTimelog(TimelogVO timelog, String userId);

	void deleteTimelog(String projectId, String issueId, String timeLogId, String userId);

	Map<String, Object> getIssueTimelogSummary(String projectId, String issueId);
}
