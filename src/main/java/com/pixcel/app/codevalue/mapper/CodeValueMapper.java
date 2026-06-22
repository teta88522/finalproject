package com.pixcel.app.codevalue.mapper;

import java.util.List;

// Mapper XML에서 파라미터 이름을 사용할 수 있게 해주는 어노테이션
import org.apache.ibatis.annotations.Param;

// 네가 만든 CodeValueVO import
import com.pixcel.app.codevalue.service.CodeValueVO;


public interface CodeValueMapper {

    /* 특정 사용자의 코드값 전체 목록을 조회한다.*/
    List<CodeValueVO> selectCodeValueList(@Param("userId") String userId);


    /* 특정 사용자 + 특정 그룹의 코드값 목록을 조회한다. */
    List<CodeValueVO> selectCodeValueListByGroup(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName
    );


    /* 코드값 ID로 코드값 상세 정보를 1개 조회한다.  */
    CodeValueVO selectCodeValueDetail(@Param("settingCodeId") Integer settingCodeId);


    /**
     * 새 코드값을 등록한다.
     *
     * CodeValueVO 안에 들어있는 값들을 XML에서 꺼내 INSERT한다.
     *
     * int 반환 의미:
     * - INSERT 성공한 row 수
     * - 정상 등록이면 보통 1 반환
     *
     * @param codeValue 등록할 코드값 정보
     * @return 등록된 row 수
     */
    int insertCodeValue(CodeValueVO codeValue);


    /**
     * 기존 코드값을 수정한다.
     *
     * settingCodeId를 기준으로 찾아서
     * settingName, defaultYn, useYn 등을 수정한다.
     *
     * @param codeValue 수정할 코드값 정보
     * @return 수정된 row 수
     */
    int updateCodeValue(CodeValueVO codeValue);


    /**
     * 코드값을 삭제한다.
     *
     * 주의:
     * - 실제 삭제 가능 여부 판단은 Mapper가 아니라 Service에서 해야 한다.
     * - 예: 기본값인지, 사용 중인지, 대체 코드값이 필요한지 검사
     *
     * @param settingCodeId 삭제할 코드값 ID
     * @return 삭제된 row 수
     */
    int deleteCodeValue(@Param("settingCodeId") Integer settingCodeId);


    /* 같은 사용자 + 같은 그룹 안에 기본값이 이미 있는지 개수를 확인한다.*/
    
    int countDefaultCodeValue(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName
    );


    /**
     * 같은 사용자 + 같은 그룹 안에서 코드값 이름이 중복되는지 확인한다.
     *
     * 예:
     * userId = "admin"
     * groupName = "일감 우선순위"
     * settingName = "높음"
     *
     * 이미 "높음"이 있으면 COUNT 결과가 1 이상 나온다.
     *
     * @param userId 사용자 ID
     * @param groupName 코드 그룹명
     * @param settingName 코드값 이름
     * @return 중복 개수
     */
    int countDuplicateSettingName(
            @Param("userId") String userId,
            @Param("settingGroupName") String settingGroupName,
            @Param("settingName") String settingName
    );
}