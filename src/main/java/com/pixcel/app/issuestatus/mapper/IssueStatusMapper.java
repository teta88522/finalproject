package com.pixcel.app.issuestatus.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.issuestatus.service.IssueStatusVO;

public interface IssueStatusMapper {

    // 사용자별 일감 상태 전체 목록을 조회한다.
    List<IssueStatusVO> selectIssueStatusList(@Param("userId") String userId);

    // 사용자별 일감 상태 ID 기준으로 상세 정보를 조회한다.
    IssueStatusVO selectIssueStatusDetail(
            @Param("issueStatusId") String issueStatusId,
            @Param("userId") String userId
    );

    // 신규 일감 상태를 등록한다.
    int insertIssueStatus(IssueStatusVO issueStatus);

    // 기존 일감 상태 정보를 수정한다.
    int updateIssueStatus(IssueStatusVO issueStatus);

    // 사용자별 일감 상태를 삭제한다.
    int deleteIssueStatus(
            @Param("issueStatusId") String issueStatusId,
            @Param("userId") String userId
    );

    // 사용자별 상태명 중복 여부를 확인한다.
    int countDuplicateStatusName(
            @Param("userId") String userId,
            @Param("statusName") String statusName,
            @Param("issueStatusId") String issueStatusId
    );

    // 일감, 일감유형, 업무흐름에서 해당 상태가 사용 중인지 확인한다.
    int countUsedIssueStatus(@Param("issueStatusId") String issueStatusId);
}