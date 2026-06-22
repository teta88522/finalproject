package com.pixcel.app.codevalue.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.codevalue.service.CodeValueVO;

public interface CodeValueMapper {

    // 코드값 전체 조회
    List<CodeValueVO> selectCodeValueList(@Param("userId") String userId);

    // 특정 그룹 코드값 목록 조회 / 머지용1
    List<CodeValueVO> selectCodeValueListByGroup(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName
    );

    // 코드값 ID 기준 단건 조회
    CodeValueVO selectCodeValueDetail(
            @Param("userId") String userId,
            @Param("settingCodeId") String settingCodeId
    );

    // 코드값 등록
    int insertCodeValue(CodeValueVO codeValue);

    // 같은 그룹 안의 코드값 이름 중복 확인
    int countDuplicateSettingName(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName,
            @Param("settingName") String settingName
    );

    // 같은 그룹 안의 기본값 개수 확인
    int countDefaultCodeValue(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName
    );

    // 특정 그룹의 기본값 전체 해제
    int updateDefaultYnToN(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName
    );

    // 특정 코드값을 기본값으로 설정
    int updateDefaultYnToY(
            @Param("userId") String userId,
            @Param("settingCodeId") String settingCodeId
    );

    // 코드값 삭제
    int deleteCodeValue(
            @Param("userId") String userId,
            @Param("settingCodeId") String settingCodeId
    );

    // 삭제 시 대체 가능한 코드값 목록 조회
    List<CodeValueVO> selectReplacementCodeValueList(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName,
            @Param("deleteSettingCodeId") String deleteSettingCodeId
    );

    // 삭제 대상 코드값을 실제 데이터가 사용 중인지 확인
    int countUsedCodeValue(@Param("settingCodeId") String settingCodeId);

    // 삭제 대상 코드값을 참조 중인 데이터를 대체 코드값으로 변경
    int updateUsedCodeValueToReplacement(
            @Param("deleteSettingCodeId") String deleteSettingCodeId,
            @Param("replaceSettingCodeId") String replaceSettingCodeId
    );
}