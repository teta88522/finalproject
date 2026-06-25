package com.pixcel.app.issues.service;

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
public class IssuesVO {

    /*
     * ISSUE 컬럼
     */
    private String issueId;
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
    private Date createdAt;
    private Date updatedAt;
    private String parentIssueId;

    /*
     * 화면 표시 / 선택 목록용 필드
     */
    private String projectName;
    private String ownerId;
    private String milestoneTitle;
    private String versionName;
    private String versionStatusCode;
    private String issueTypeName;
    private String initialStatusId;
    private String initialStatusName;
    private String issueStatusName;
    private String settingName;
    private String defaultYn;
    private String assigneeName;
    private String authorName;
    private String parentIssueTitle;

    /*
     * ISSUE_TYPE_FIELD_SETTING 조회용 필드
     */
    private String fieldSettingId;
    private String fieldCode;
    private String useYn;
    private String requiredYn;
}
