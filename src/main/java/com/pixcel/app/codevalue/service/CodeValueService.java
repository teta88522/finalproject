package com.pixcel.app.codevalue.service;

import java.util.List;

public interface CodeValueService {

    // 특정 사용자의 코드값 전체 목록 조회
    List<CodeValueVO> getCodeValueList(String userId);

    // 특정 그룹의 코드값 목록 조회
    List<CodeValueVO> getCodeValueListByGroup(String userId, String settingGroupName);

    // 코드값 상세 조회
    CodeValueVO getCodeValueDetail(Integer settingCodeId);

    // 코드값 등록
    void createCodeValue(CodeValueVO codeValue);

    // 코드값 수정
    void modifyCodeValue(CodeValueVO codeValue);

    // 코드값 삭제
    void removeCodeValue(Integer settingCodeId);
}