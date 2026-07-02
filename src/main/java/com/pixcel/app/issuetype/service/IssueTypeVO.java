package com.pixcel.app.issuetype.service;

import java.util.Date;
import java.util.List;

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
public class IssueTypeVO {

    /*
     * ISSUE_TYPE 컬럼
     */
    private String issueTypeId;

    private String initialStatusId;

    private List<String> initialStatusIdList;

    private String issueTypeName;

    // 화면에서는 제외. DB 컬럼은 남아 있으므로 값은 null로 관리한다.
    private String roadmapYn;

    private String description;

    private Date createdAt;

    private Date updatedAt;

    // 관리자/설정 소유자 USER_ID
    private String userId;


    /*
     * ISSUE_TYPE_PROJECT 컬럼
     */
    private String issueTypeProjectId;

    private String projectId;


    /*
     * ISSUE_TYPE_FIELD_SETTING 컬럼
     */
    private String fieldSettingId;

    private String fieldCode;

    private String useYn;

    private String requiredYn;


    /*
     * 화면 조회 표시용 필드
     * DB 컬럼은 아니고 JOIN / LISTAGG 결과를 담는다.
     */
    private String initialStatusName;

    private String issueStatusId;

    private String statusName;

    private String projectName;

    private String projectNames;

    private String fieldSummary;


    /*
     * 생성 / 복사 화면 form 전송용 필드
     */
    private List<String> projectIdList;

    private List<String> usedProjectIdList;

    private List<String> fieldCodeList;

    private List<String> requiredFieldCodeList;

    private List<IssueTypeVO> fieldSettingList;
    
    /*
     * 요약 화면 조회용 필드
     */
    private List<String> issueTypeIdList;

    private String assigneeYn;

    private String milestoneYn;

    private String parentIssueYn;

    private String startDateYn;

    private String dueDateYn;

    private String estimatedHoursYn;

    private String optionGroup;

    private Integer duplicateCount;

    private Integer initialStatusCount;

    private Integer selectedProjectCount;

    private Integer usedCount;

    private Integer sortNo;
}
