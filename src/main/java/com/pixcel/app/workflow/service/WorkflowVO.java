package com.pixcel.app.workflow.service;

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
public class WorkflowVO {

    /*
     * WORKFLOW_TRANSITION 컬럼
     */
    private String transitionId;

    private String issueTypeId;

    private String roleId;

    private String fromStatusId;

    private String toStatusId;

    // j001: 담당자, j002: 작성자, j003: 기본
    private String applyTargetCode;

    // 설정 생성자. 권한/소유자 판단 기준이 아니라 기록용이다.
    private String createdBy;

    private Date createdAt;


    /*
     * 화면 / 소유자 검증용
     */
    private String userId;

    private String issueTypeName;

    private String roleName;

    private String issueStatusId;

    private String statusName;

    private String closedYn;

    private String fromStatusName;

    private String toStatusName;

    private String applyTargetName;


    /*
     * 상태전환 설정 화면 form 전송용    -> 체크박스 선택항목 
     * 예: FROM_STATUS_ID|TO_STATUS_ID
     */
    private String transitionKey;

    private List<String> transitionKeyList;

    // 상태 소유자 검증용   -> 개발자 도구같은 것을 이용해 임의로 바꿀수 있기 때문 
    private List<String> statusIdList;
}