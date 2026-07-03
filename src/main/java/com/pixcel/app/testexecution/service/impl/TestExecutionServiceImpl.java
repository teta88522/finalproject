package com.pixcel.app.testexecution.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.testexecution.mapper.TestExecutionMapper;
import com.pixcel.app.testexecution.service.TestExecutionService;
import com.pixcel.app.testexecution.service.TestExecutionVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestExecutionServiceImpl implements TestExecutionService {

	private final TestExecutionMapper testExecutionMapper;

	@Override
	public TestExecutionVO selectTestExecutionInfo(String projectId, String testId) {
		return testExecutionMapper.selectTestExecutionInfo(projectId, testId);
	}

	@Override
	public List<TestExecutionVO> selectExecutionCaseList(String testId) {
		return testExecutionMapper.selectExecutionCaseList(testId);
	}

	@Override
	public int selectCurrentRetryNo(String mappingId) {
		return testExecutionMapper.selectCurrentRetryNo(mappingId);
	}

	@Override
	@Transactional
	public int insertTestExecution(TestExecutionVO testExecutionVO) {

		/*
		 * 저장 전 retry_no 계산
		 * - 미수행(o001): 0
		 * - 미수행이 아닌 상태: 기존 retry_no + 1
		 */
		applyRetryNo(testExecutionVO);

		/*
		 * 이미 해당 mapping_id로 저장된 실행결과가 있으면 update
		 * 없으면 insert
		 */
		String executionId = testExecutionMapper.selectExecutionIdByMappingId(testExecutionVO.getMappingId());

		if (executionId != null && !"".equals(executionId)) {
			testExecutionVO.setExecutionId(executionId);
			return testExecutionMapper.updateTestExecution(testExecutionVO);
		}

		return testExecutionMapper.insertTestExecution(testExecutionVO);
	}

	@Override
	@Transactional
	public int insertTestExecutionList(TestExecutionVO testExecutionVO) {

		int result = 0;

		List<String> mappingIdList = testExecutionVO.getMappingIdList();
		List<String> resultStatusCodeList = testExecutionVO.getResultStatusCodeList();
		List<String> executionCommentList = testExecutionVO.getExecutionCommentList();

		if (mappingIdList == null || mappingIdList.isEmpty()) {
			return 0;
		}

		for (int i = 0; i < mappingIdList.size(); i++) {

			TestExecutionVO item = new TestExecutionVO();

			item.setMappingId(mappingIdList.get(i));
			item.setExecutorId(testExecutionVO.getExecutorId());

			if (resultStatusCodeList != null && resultStatusCodeList.size() > i) {
				item.setResultStatusCode(resultStatusCodeList.get(i));
			} else {
				item.setResultStatusCode("o001");
			}

			if (executionCommentList != null && executionCommentList.size() > i) {
				item.setExecutionComment(executionCommentList.get(i));
			}

			/*
			 * 각 행마다 retry_no 계산
			 */
			applyRetryNo(item);

			/*
			 * 이미 저장된 실행결과가 있으면 update
			 * 없으면 insert
			 */
			String executionId = testExecutionMapper.selectExecutionIdByMappingId(item.getMappingId());

			if (executionId != null && !"".equals(executionId)) {
				item.setExecutionId(executionId);
				result += testExecutionMapper.updateTestExecution(item);
			} else {
				result += testExecutionMapper.insertTestExecution(item);
			}
		}

		return result;
	}

	/*
	 * retry_no 계산 공통 메서드
	 *
	 * 요구사항:
	 * 1. 미수행(o001)으로 저장하면 retry_no = 0
	 * 2. 미수행이 아닌 상태로 저장하면 현재 retry_no + 1
	 * 3. 성공/실패/진행중 상태에서 미수행으로 변경하면 retry_no = 0으로 초기화
	 */
	private void applyRetryNo(TestExecutionVO testExecutionVO) {

		String resultStatusCode = testExecutionVO.getResultStatusCode();

		if ("o001".equals(resultStatusCode)) {
			testExecutionVO.setRetryNo(0);
			return;
		}

		int currentRetryNo = testExecutionMapper.selectCurrentRetryNo(testExecutionVO.getMappingId());

		testExecutionVO.setRetryNo(currentRetryNo + 1);
	}
}