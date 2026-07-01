package com.pixcel.app.workhistory.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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
public class WorkhistoryVO {

	/*
	 * 화면 공통 / 프로젝트 접근 확인용
	 */
	private String projectId;
	private String projectName;
	private String ownerId;
	private String userId;

	/*
	 * 작업내역 목록 검색 조건
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	private String keyword;
	private List<String> changeTypeCodeList;
	private List<String> fieldCodeList;
	private List<String> changedByList;
	private List<String> issueTypeIdList;
	private List<String> issueStatusIdList;
	private List<String> settingCodeIdList;

	/*
	 * 페이징
	 * 전체 COUNT(*) 없이 현재 페이지 블록 확인용 ID 조회에 사용한다.
	 */
	private Integer page;
	private Integer pageSize;
	private Integer startRow;
	private Integer endRow;
	private Integer rowNo;
	private Boolean hasPreviousPage;
	private Boolean hasNextPage;
	private List<Integer> pageList;

	/*
	 * ISSUE_HISTORY 기본 정보
	 */
	private String historyId;
	private String historyGroupId;
	private String changedBy;
	private String changedByName;
	private String changeTypeCode;
	private String changeTypeName;
	private String changeTypeDisplayName;
	private String fieldCode;
	private String fieldName;
	private String beforeValue;
	private String afterValue;
	private String reason;
	private Date changedAt;

	/*
	 * 연결 일감 표시 정보
	 */
	private String issueId;
	private Integer issueNo;
	private String displayIssueNo;
	private String issueTitle;
	private String issueTypeId;
	private String issueTypeName;
	private String issueStatusId;
	private String issueStatusName;
	private String settingCodeId;
	private String settingName;
	private String assigneeId;
	private String assigneeName;

	/*
	 * 필터 옵션 / 선택값 표시용
	 * XML에서 여러 옵션을 UNION ALL로 내려줄 때 optionGroup으로 분리한다.
	 */
	private String optionGroup;
	private Integer optionSort;
	private Integer rowSort;
	private String optionValue;
	private String optionLabel;
}
