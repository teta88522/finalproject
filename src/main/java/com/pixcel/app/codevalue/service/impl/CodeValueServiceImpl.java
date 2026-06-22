package com.pixcel.app.codevalue.service.impl;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.codevalue.mapper.CodeValueMapper;
import com.pixcel.app.codevalue.service.CodeValueService;
import com.pixcel.app.codevalue.service.CodeValueVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeValueServiceImpl implements CodeValueService {

    private final CodeValueMapper codeValueMapper;

    // 특정 사용자의 코드값 전체 목록 조회
    @Override
    public List<CodeValueVO> getCodeValueList(String userId) {
        return codeValueMapper.selectCodeValueList(userId);
    }

    // 특정 그룹의 코드값 목록 조회
    @Override
    public List<CodeValueVO> getCodeValueListByGroup(String userId, String settingGroupName) {
        return codeValueMapper.selectCodeValueListByGroup(userId, settingGroupName);
    }

    // 코드값 상세 조회
    @Override
    public CodeValueVO getCodeValueDetail(Integer settingCodeId) {
        return codeValueMapper.selectCodeValueDetail(settingCodeId);
    }

    // 코드값 등록
    @Override
    @Transactional
    public void createCodeValue(CodeValueVO codeValue) {

        int duplicateCount = codeValueMapper.countDuplicateSettingName(
                codeValue.getUserId(),
                codeValue.getSettingGroupName(),
                codeValue.getSettingName()
        );

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 존재하는 코드값 이름입니다.");
        }

        if ("Y".equals(codeValue.getDefaultYn())) {
            int defaultCount = codeValueMapper.countDefaultCodeValue(
                    codeValue.getUserId(),
                    codeValue.getSettingGroupName()
            );

            if (defaultCount > 0) {
                throw new IllegalArgumentException("해당 그룹에는 이미 기본값이 존재합니다.");
            }
        }

        codeValueMapper.insertCodeValue(codeValue);
    }

    // 코드값 수정
    @Override
    @Transactional
    public void modifyCodeValue(CodeValueVO codeValue) {

        if ("Y".equals(codeValue.getDefaultYn())) {
            int defaultCount = codeValueMapper.countDefaultCodeValue(
                    codeValue.getUserId(),
                    codeValue.getSettingGroupName()
            );

            if (defaultCount > 0) {
                throw new IllegalArgumentException("해당 그룹에는 이미 기본값이 존재합니다.");
            }
        }

        codeValueMapper.updateCodeValue(codeValue);
    }

    // 코드값 삭제
    @Override
    @Transactional
    public void removeCodeValue(Integer settingCodeId) {
        codeValueMapper.deleteCodeValue(settingCodeId);
    }
}