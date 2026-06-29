package com.pixcel.app.issuetype.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.issuetype.service.IssueTypeVO;

public interface IssueTypeMapper {

	List<IssueTypeVO> selectIssueTypeListPageRows(IssueTypeVO searchVO);

	List<IssueTypeVO> selectIssueTypeFormOptionRows(@Param("userId") String userId);

	// 검색조건에 맞는 사용자별 일감유형 목록을 조회한다.
	List<IssueTypeVO> selectIssueTypeList(IssueTypeVO searchVO);
	
	// 일감유형별 표준 항목 사용 여부 요약 목록을 조회한다.
	List<IssueTypeVO> selectIssueTypeSummaryList(IssueTypeVO searchVO);

    // 사용자별 일감유형 상세 정보를 조회한다.
    IssueTypeVO selectIssueTypeDetail(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 현재 사용자가 소유한 프로젝트 목록을 조회한다.
    List<IssueTypeVO> selectProjectList(@Param("userId") String userId);

    // 일감유형에 적용된 프로젝트 ID 목록을 조회한다.
    List<String> selectAppliedProjectIdList(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 일감유형에서 사용 처리된 항목 코드를 조회한다.
    List<String> selectUsedFieldCodeList(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 일감유형에서 필수 처리된 항목 코드를 조회한다.
    List<String> selectRequiredFieldCodeList(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    List<IssueTypeVO> selectFieldSettingListByIssueType(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    IssueTypeVO selectIssueTypeSaveValidation(IssueTypeVO issueType);

    IssueTypeVO selectIssueTypeDeleteCheck(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 신규 일감유형을 등록한다.
    int insertIssueType(IssueTypeVO issueType);

    // 일감유형 적용 프로젝트를 등록한다.
    int insertIssueTypeProject(IssueTypeVO issueType);

    int insertIssueTypeProjectList(IssueTypeVO issueType);

    // 일감유형 항목 설정을 등록한다.
    int insertIssueTypeFieldSetting(IssueTypeVO issueType);

    int insertIssueTypeFieldSettingList(IssueTypeVO issueType);

    // 사용자별 일감유형명 중복 여부를 확인한다.
    int countDuplicateIssueTypeName(
            @Param("userId") String userId,
            @Param("issueTypeName") String issueTypeName
    );

    // 선택한 초기 상태가 현재 사용자의 상태인지 확인한다.
    int countInitialStatusByUser(
            @Param("initialStatusId") String initialStatusId,
            @Param("userId") String userId
    );

    // 선택한 프로젝트들이 현재 사용자의 프로젝트인지 확인한다.
    int countSelectedProjectByUser(
            @Param("userId") String userId,
            @Param("projectIdList") List<String> projectIdList
    );

    // 일감유형이 일감 또는 업무흐름에서 사용 중인지 확인한다.
    int countUsedIssueType(@Param("issueTypeId") String issueTypeId);

    // 일감유형 적용 프로젝트를 삭제한다.
    int deleteIssueTypeProjectByIssueTypeId(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 일감유형 항목 설정을 삭제한다.
    int deleteIssueTypeFieldSettingByIssueTypeId(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );

    // 사용자가 소유한 일감유형을 삭제한다.
    int deleteIssueType(
            @Param("issueTypeId") String issueTypeId,
            @Param("userId") String userId
    );
}
