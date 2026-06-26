package com.pixcel.app.testcase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.testcase.service.TestCaseSearchVO;
import com.pixcel.app.testcase.service.TestCaseVO;

@Mapper
public interface TestCaseMapper {
    // 테스트 생성 화면용 테스트케이스 총 개수
    public int selectTestCaseCount(TestCaseSearchVO testCaseSearchVO);

    // 테스트 생성 화면용 테스트케이스 목록
    public List<TestCaseVO> selectTestCaseList(TestCaseSearchVO testCaseSearchVO);
}
