package com.pixcel.app.codevalue.service.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	// 코드값 화면에서 사용할 그룹
	private static final String GROUP_G001 = "g001";
	private static final String GROUP_G002 = "g002";
	private static final String GROUP_G003 = "g003";

	// 코드값 전체 조회
	@Override
	public List<CodeValueVO> getCodeValueList(String userId) {
		validateUserId(userId);
		return codeValueMapper.selectCodeValueList(userId);
	}

	// 검색조건에 맞는 사용자별 코드값 목록 조회
	@Override
	public List<CodeValueVO> getCodeValueSearchList(CodeValueVO searchVO) {

		if (searchVO == null) {
			throw new IllegalArgumentException("검색조건 정보가 없습니다.");
		}

		validateUserId(searchVO.getUserId());
		normalizeSearchCondition(searchVO);

		return codeValueMapper.selectCodeValueSearchList(searchVO);
	}

	// 특정 그룹 코드값 조회
	@Override
	public List<CodeValueVO> getCodeValueListByGroup(String userId, String settingGroupName) {
		validateUserId(userId);
		return codeValueMapper.selectCodeValueListByGroup(userId, settingGroupName);
	}

	// 화면 출력용 그룹별 코드값 조회
	@Override
	public Map<String, List<CodeValueVO>> getCodeValueGroupMap(String userId) {
		validateUserId(userId);

		Map<String, List<CodeValueVO>> groupMap = new LinkedHashMap<>();

		groupMap.put(GROUP_G001, codeValueMapper.selectCodeValueListByGroup(userId, GROUP_G001));
		groupMap.put(GROUP_G002, codeValueMapper.selectCodeValueListByGroup(userId, GROUP_G002));
		groupMap.put(GROUP_G003, codeValueMapper.selectCodeValueListByGroup(userId, GROUP_G003));

		return groupMap;
	}

	// 검색 결과를 화면 출력용 그룹별 코드값으로 변환
	@Override
	public Map<String, List<CodeValueVO>> getCodeValueSearchGroupMap(CodeValueVO searchVO) {

		String selectedGroupName = searchVO.getSettingGroupName();
		List<String> selectedGroupNameList = searchVO.getSettingGroupNameList();
		List<CodeValueVO> codeValueList;

		searchVO.setSettingGroupName(null);
		searchVO.setSettingGroupNameList(null);

		try {
			codeValueList = getCodeValueSearchList(searchVO);
		} finally {
			searchVO.setSettingGroupName(selectedGroupName);
			searchVO.setSettingGroupNameList(selectedGroupNameList);
		}

		Map<String, List<CodeValueVO>> groupMap = new LinkedHashMap<>();

		groupMap.put(GROUP_G001, filterByGroup(codeValueList, GROUP_G001));
		groupMap.put(GROUP_G002, filterByGroup(codeValueList, GROUP_G002));
		groupMap.put(GROUP_G003, filterByGroup(codeValueList, GROUP_G003));

		return groupMap;
	}

	private void normalizeSearchCondition(CodeValueVO searchVO) {
		searchVO.setSettingGroupNameList(normalizeSearchList(searchVO.getSettingGroupNameList(),
				searchVO.getSettingGroupName()));
		searchVO.setDefaultYnList(normalizeSearchList(searchVO.getDefaultYnList(), searchVO.getDefaultYn()));
	}

	private List<String> normalizeSearchList(List<String> valueList, String legacyValue) {
		List<String> normalizedList = valueList == null ? Collections.emptyList() : valueList.stream()
				.map(this::trimToNull)
				.filter(value -> value != null)
				.distinct()
				.collect(Collectors.toList());

		if (!normalizedList.isEmpty()) {
			return normalizedList;
		}

		String checkedLegacyValue = trimToNull(legacyValue);

		if (checkedLegacyValue == null) {
			return Collections.emptyList();
		}

		return Collections.singletonList(checkedLegacyValue);
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isEmpty() ? null : trimmedValue;
	}

	// 코드값 단건 조회
	@Override
	public CodeValueVO getCodeValueDetail(String settingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO codeValue = codeValueMapper.selectCodeValueDetail(userId, settingCodeId);

		if (codeValue == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		return codeValue;
	}

	// 코드값 등록
	@Override
	@Transactional
	public void createCodeValue(CodeValueVO codeValue, String userId) {
		validateUserId(userId);
		validateCreateCodeValue(codeValue);

		codeValue.setUserId(userId);

		if (codeValue.getDefaultYn() == null || codeValue.getDefaultYn().trim().isEmpty()) {
			codeValue.setDefaultYn("N");
		}

		int duplicateCount = codeValueMapper.countDuplicateSettingName(
				userId,
				codeValue.getSettingGroupName(),
				codeValue.getSettingName()
		);

		if (duplicateCount > 0) {
			throw new IllegalArgumentException("이미 존재하는 코드값 이름입니다.");
		}

		if ("Y".equals(codeValue.getDefaultYn())) {
			int defaultCount = codeValueMapper.countDefaultCodeValue(userId, codeValue.getSettingGroupName());

			if (defaultCount > 0) {
				throw new IllegalArgumentException("해당 그룹에는 이미 기본값이 존재합니다.");
			}
		}

		/*
		 * useYn은 여기서 세팅하지 않는다.
		 * 이 화면에서는 사용 여부를 관리하지 않고,
		 * DB DEFAULT 'N'이 적용되도록 insert SQL에서 use_yn 컬럼을 제외한다.
		 */
		codeValueMapper.insertCodeValue(codeValue);
	}

	// 기본값 설정
	@Override
	@Transactional
	public void setDefaultCodeValue(String settingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO target = codeValueMapper.selectCodeValueDetail(userId, settingCodeId);

		if (target == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		/*
		 * USE_YN은 이 화면의 관리 대상이 아니므로 검사하지 않는다.
		 * 기본값 설정은 같은 그룹 안에서 하나만 존재하도록 처리한다.
		 */
		codeValueMapper.updateDefaultYnToN(userId, target.getSettingGroupName());
		codeValueMapper.updateDefaultYnToY(userId, settingCodeId);
	}

	// 기본값 취소
	@Override
	@Transactional
	public void cancelDefaultCodeValue(String settingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO target = codeValueMapper.selectCodeValueDetail(userId, settingCodeId);

		if (target == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		codeValueMapper.updateDefaultYnToN(userId, target.getSettingGroupName());
	}

	// 삭제 시 대체값 선택이 필요한지 확인
	@Override
	public boolean isReplacementRequired(String settingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO target = codeValueMapper.selectCodeValueDetail(userId, settingCodeId);

		if (target == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		int usedCount = codeValueMapper.countUsedCodeValue(settingCodeId);

		return "Y".equals(target.getDefaultYn()) || usedCount > 0;
	}

	// 삭제 시 대체 가능한 코드값 목록 조회
	@Override
	public List<CodeValueVO> getReplacementCodeValueList(String deleteSettingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO deleteTarget = codeValueMapper.selectCodeValueDetail(userId, deleteSettingCodeId);

		if (deleteTarget == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		return codeValueMapper.selectReplacementCodeValueList(
				userId,
				deleteTarget.getSettingGroupName(),
				deleteSettingCodeId
		);
	}

	// 코드값 단순 삭제
	@Override
	@Transactional
	public void removeCodeValue(String settingCodeId, String userId) {
		validateUserId(userId);

		CodeValueVO target = codeValueMapper.selectCodeValueDetail(userId, settingCodeId);

		if (target == null) {
			throw new IllegalArgumentException("존재하지 않는 코드값입니다.");
		}

		int usedCount = codeValueMapper.countUsedCodeValue(settingCodeId);

		if ("Y".equals(target.getDefaultYn()) || usedCount > 0) {
			throw new IllegalStateException("대체 코드값 선택이 필요한 코드값입니다.");
		}

		codeValueMapper.deleteCodeValue(userId, settingCodeId);
	}

	// 대체 코드값으로 변경 후 삭제
	@Override
	@Transactional
	public void removeCodeValueWithReplacement(String deleteSettingCodeId,
											   String replaceSettingCodeId,
											   String userId) {
		validateUserId(userId);

		if (replaceSettingCodeId == null || replaceSettingCodeId.trim().isEmpty()) {
			throw new IllegalArgumentException("대체 코드값을 선택해야 합니다.");
		}

		if (deleteSettingCodeId.equals(replaceSettingCodeId)) {
			throw new IllegalArgumentException("삭제 대상과 대체 코드값은 같을 수 없습니다.");
		}

		CodeValueVO deleteTarget = codeValueMapper.selectCodeValueDetail(userId, deleteSettingCodeId);
		CodeValueVO replaceTarget = codeValueMapper.selectCodeValueDetail(userId, replaceSettingCodeId);

		if (deleteTarget == null) {
			throw new IllegalArgumentException("삭제 대상 코드값이 존재하지 않습니다.");
		}

		if (replaceTarget == null) {
			throw new IllegalArgumentException("대체 코드값이 존재하지 않습니다.");
		}

		if (!deleteTarget.getSettingGroupName().equals(replaceTarget.getSettingGroupName())) {
			throw new IllegalArgumentException("같은 그룹의 코드값으로만 대체할 수 있습니다.");
		}

		/*
		 * TODO 실제 참조 테이블이 확정되면 이 메서드에서 참조 데이터를 대체 코드값으로 변경한다.
		 * 현재 XML은 임시 SQL이므로 실제 변경되는 데이터는 없다.
		 */
		codeValueMapper.updateUsedCodeValueToReplacement(deleteSettingCodeId, replaceSettingCodeId);

		/*
		 * 삭제 대상이 기본값이면 대체 코드값을 기본값으로 승격한다.
		 * 그룹당 기본값은 하나만 가능하므로 기존 기본값을 모두 N 처리 후 대체값만 Y 처리한다.
		 */
		if ("Y".equals(deleteTarget.getDefaultYn())) {
			codeValueMapper.updateDefaultYnToN(userId, deleteTarget.getSettingGroupName());
			codeValueMapper.updateDefaultYnToY(userId, replaceSettingCodeId);
		}

		codeValueMapper.deleteCodeValue(userId, deleteSettingCodeId);
	}

	// 특정 그룹의 코드값 목록만 반환한다.
	private List<CodeValueVO> filterByGroup(List<CodeValueVO> codeValueList, String settingGroupName) {

		return codeValueList.stream()
				.filter(codeValue -> settingGroupName.equals(codeValue.getSettingGroupName()))
				.collect(Collectors.toList());
	}

	// 사용자 ID를 검증한다.
	private void validateUserId(String userId) {

		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("로그인 정보가 없습니다.");
		}
	}

	// 등록값 검증
	private void validateCreateCodeValue(CodeValueVO codeValue) {
		if (codeValue == null) {
			throw new IllegalArgumentException("등록할 코드값 정보가 없습니다.");
		}

		if (codeValue.getSettingGroupName() == null || codeValue.getSettingGroupName().trim().isEmpty()) {
			throw new IllegalArgumentException("코드값 그룹이 필요합니다.");
		}

		if (codeValue.getSettingName() == null || codeValue.getSettingName().trim().isEmpty()) {
			throw new IllegalArgumentException("코드값 이름이 필요합니다.");
		}

		if (codeValue.getDefaultYn() != null
				&& !"Y".equals(codeValue.getDefaultYn())
				&& !"N".equals(codeValue.getDefaultYn())) {
			throw new IllegalArgumentException("기본값 여부 값이 올바르지 않습니다.");
		}
	}
}
