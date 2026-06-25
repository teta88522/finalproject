package com.pixcel.app.issues.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.issues.service.IssuesVO;

public interface IssuesMapper {

    // 현재 사용자가 접근 가능한 프로젝트 목록을 조회한다.
    List<IssuesVO> selectProjectList(@Param("userId") String userId);

    // 현재 사용자가 접근 가능한 프로젝트 단건을 조회한다.
    IssuesVO selectProjectDetailForUser(
            @Param("projectId") String projectId,
            @Param("userId") String userId
    );

    // 프로젝트 생성자이거나 일감 추가 권한이 있는지 확인한다.
    int countIssueCreatePermission(
            @Param("projectId") String projectId,
            @Param("userId") String userId
    );

    // 프로젝트에 적용된 일감유형 목록을 조회한다.
    List<IssuesVO> selectIssueTypeList(@Param("projectId") String projectId);

    // 생성에 사용할 일감유형 상세 정보를 조회한다.
    IssuesVO selectIssueTypeDetailForCreate(
            @Param("projectId") String projectId,
            @Param("issueTypeId") String issueTypeId
    );

    // 프로젝트에 적용된 일감유형별 표준 항목 설정 목록을 조회한다.
    List<IssuesVO> selectFieldSettingListByProject(@Param("projectId") String projectId);

    // 일감유형의 표준 항목 설정 목록을 조회한다.
    List<IssuesVO> selectFieldSettingListByIssueType(@Param("issueTypeId") String issueTypeId);

    // 생성 가능한 버전 목록을 조회한다.
    List<IssuesVO> selectVersionList(@Param("projectId") String projectId);

    // 생성 가능한 마일스톤 목록을 조회한다.
    List<IssuesVO> selectMilestoneList(@Param("projectId") String projectId);

    // 프로젝트 소유자가 관리하는 일감 우선순위 코드값 목록을 조회한다.
    List<IssuesVO> selectPriorityList(@Param("projectId") String projectId);

    // 프로젝트 구성원 목록을 담당자 후보로 조회한다.
    List<IssuesVO> selectAssigneeList(@Param("projectId") String projectId);

    // 같은 프로젝트 안의 일감 목록을 상위일감 후보로 조회한다.
    List<IssuesVO> selectParentIssueList(@Param("projectId") String projectId);

    // 선택한 버전이 생성 가능한 버전인지 확인한다.
    int countVersionForCreate(
            @Param("projectId") String projectId,
            @Param("versionId") String versionId
    );

    // 선택한 우선순위가 프로젝트 소유자의 코드값인지 확인한다.
    int countPriorityForProject(
            @Param("projectId") String projectId,
            @Param("settingCodeId") String settingCodeId
    );

    // 선택한 마일스톤이 프로젝트와 버전에 속하는지 확인한다.
    int countMilestoneForCreate(
            @Param("projectId") String projectId,
            @Param("versionId") String versionId,
            @Param("milestoneId") String milestoneId
    );

    // 선택한 담당자가 프로젝트 구성원인지 확인한다.
    int countAssigneeForProject(
            @Param("projectId") String projectId,
            @Param("assigneeId") String assigneeId
    );

    // 선택한 상위일감이 같은 프로젝트 일감인지 확인한다.
    int countParentIssueForProject(
            @Param("projectId") String projectId,
            @Param("parentIssueId") String parentIssueId
    );

    // 신규 일감을 등록한다.
    int insertIssue(IssuesVO issue);
}
