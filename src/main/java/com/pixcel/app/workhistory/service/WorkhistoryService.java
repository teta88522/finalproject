package com.pixcel.app.workhistory.service;

import java.util.Map;

public interface WorkhistoryService {

	Map<String, Object> getWorkhistoryPageData(String projectId, WorkhistoryVO searchVO, String userId);

	Map<String, Object> getWorkhistoryFilterOptions(String projectId, String userId);
}
