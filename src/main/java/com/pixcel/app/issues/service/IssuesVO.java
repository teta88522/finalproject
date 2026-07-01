package com.pixcel.app.issues.service;

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
public class IssuesVO {

	/*
	 * ISSUE 테이블 컬럼
	 */
	private String issueId;
	private Integer issueNo;
	private String projectId;
	private String milestoneId;
	private String versionId;
	private String issueTypeId;
	private String issueStatusId;
	private String settingCodeId;
	private String assigneeId;
	private String authorId;
	private String title;
	private String description;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dueDate;

	private Integer estimatedHours;
	private Integer progressRate;
	private String progressRange;
	private Integer progressMin;
	private Integer progressMax;
	private Date createdAt;
	private Date updatedAt;
	private String parentIssueId;

	/*
	 * 화면 표시 / 선택 목록용 필드
	 */
	private String displayIssueNo;

	private String projectName;
	private String ownerId;

	private String milestoneTitle;
	private String versionName;
	private String versionStatusCode;

	private String issueTypeName;
	private String initialStatusId;
	private String initialStatusName;

	private String issueStatusName;
	private String closedYn;

	/*
	 * SETTING_CODE 관련 필드 현재 ISSUE.SETTING_CODE_ID는 일감 우선순위 코드로 사용 중
	 */
	private String settingName;
	private String defaultYn;

	private String assigneeName;
	private String authorName;
	private String parentIssueTitle;
	private String optionGroup;
	private String permissionYn;
	private String issueCreatePermissionYn;
	private String issueUpdatePermissionYn;
	private String issueDeletePermissionYn;
	private String milestoneCreatePermissionYn;
	private String assigneeUseYn;
	private String assigneeRequiredYn;
	private String milestoneUseYn;
	private String milestoneRequiredYn;
	private String parentIssueUseYn;
	private String parentIssueRequiredYn;
	private String startDateUseYn;
	private String startDateRequiredYn;
	private String dueDateUseYn;
	private String dueDateRequiredYn;
	private String estimatedHoursUseYn;
	private String estimatedHoursRequiredYn;
	private Integer versionCount;
	private Integer priorityCount;
	private Integer assigneeCount;
	private Integer milestoneCount;
	private Integer parentIssueCount;

	/*
	 * 일감 전체조회 검색 조건 - 제목 검색은 title 컬럼 대상 - 날짜 검색은 startDate, dueDate를 사용한다.
	 */
	private String keyword;

	/*
	 * 일감유형별 표준항목 설정 조회용 필드.
	 * 작업내역 조회에서는 변경된 필드 코드로도 재사용한다.
	 */
	private String fieldSettingId;
	private String fieldCode;
	private String useYn;
	private String requiredYn;

	/*
	 * 일감 전체조회 정렬 조건 issueNoSort: asc / desc
	 */
	private String issueNoSort;
	private List<String> issueTypeIdList;
	private List<String> versionIdList;
	private List<String> milestoneIdList;
	private List<String> issueStatusIdList;
	private List<String> settingCodeIdList;
	private List<String> assigneeIdList;
	private List<String> progressRangeList;

	/*
	 * 일감 보고서 집계용 필드
	 */
	private Integer openCount;
	private Integer closedCount;
	private Integer totalCount;
	private Integer optionSort;
	private Integer rowSort;
	private Integer timeLogTotalCount;
	private Integer timeLogTotalHours;

	/*
	 * ISSUE_HISTORY fields
	 */
	private String historyId;
	private String historyGroupId;
	private String changedBy;
	private String changedByName;
	private String changeTypeCode;
	private String changeTypeDisplayName;
	private String fieldName;
	private String reason;
	private String historyReason;
	private String beforeValue;
	private String afterValue;
	private Date changedAt;
	private String transitionAllowedYn;
	
//	페이징
	private Integer page;
	private Integer pageSize;
	private Integer startRow;
	private Integer endRow;
	private Integer rowNo;
	private Boolean hasPreviousPage;
	private Boolean hasNextPage;
	private List<Integer> pageList;

 // TIME_LOG 조회용
    private Integer hours;
	
}
