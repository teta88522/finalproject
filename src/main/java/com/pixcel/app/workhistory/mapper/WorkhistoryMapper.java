package com.pixcel.app.workhistory.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.workhistory.service.WorkhistoryVO;

public interface WorkhistoryMapper {

	/*
	 * 프로젝트 접근 확인 + 화면 상단 프로젝트 정보 조회.
	 *
	 * XML에서 불러올 정보:
	 * - project_id
	 * - project_name
	 * - owner_id
	 *
	 * 조건:
	 * - project.project_id = projectId
	 * - 로그인 사용자가 프로젝트 owner 이거나 project_member/team_member에 포함되어야 한다.
	 */
	WorkhistoryVO selectProjectAccess(
			@Param("projectId") String projectId,
			@Param("userId") String userId);

	/*
	 * 작업내역 페이징용 ID 선조회.
	 *
	 * XML에서 불러올 정보:
	 * - history_id
	 * - row_no
	 *
	 * 핵심:
	 * - 전체 COUNT(*) 금지
	 * - project_id로 대상 일감 범위를 제한
	 * - startRow ~ endRow 범위만 확인
	 * - order by changed_at desc, history_id desc
	 * - 상세 join 전에 history_id만 먼저 가져온다.
	 */
	List<WorkhistoryVO> selectWorkhistoryPageIds(WorkhistoryVO searchVO);

	/*
	 * 현재 페이지에 표시할 작업내역 상세 row 조회.
	 *
	 * XML에서 불러올 정보:
	 * - history_id, history_group_id
	 * - changed_at, changed_by, changed_by_name
	 * - change_type_code, change_type_display_name
	 * - field_code, field_name
	 * - before_value, after_value, reason
	 * - issue_id, issue_no, display_issue_no, issue_title
	 * - issue_type_id, issue_type_name
	 * - issue_status_id, issue_status_name
	 * - setting_code_id, setting_name
	 *
	 * 핵심:
	 * - selectWorkhistoryPageIds 결과의 historyIdList만 상세 조회
	 * - 불필요한 전체 join 금지
	 * - 화면 컬럼에 필요한 값만 select
	 */
	List<WorkhistoryVO> selectWorkhistoryRowsByIds(
			@Param("projectId") String projectId,
			@Param("historyIdList") List<String> historyIdList);

	/*
	 * 선택된 필터 값의 표시명 조회.
	 *
	 * XML에서 option_group, option_value, option_label 형태로 불러온다.
	 * 예:
	 * - option_group = CHANGE_TYPE / FIELD / CHANGED_BY / ISSUE_TYPE / STATUS / PRIORITY
	 * - option_value = 실제 검색값
	 * - option_label = 화면에 보여줄 이름
	 *
	 * 핵심:
	 * - 선택된 필터가 있을 때만 실행
	 * - 필터 전체 옵션 조회와 분리
	 */
	List<WorkhistoryVO> selectSelectedFilterRows(WorkhistoryVO searchVO);

	/*
	 * 작업내역 필터 옵션 Ajax 조회.
	 *
	 * XML에서 option_group, option_value, option_label 형태로 불러온다.
	 * 예:
	 * - CHANGE_TYPE: 생성/수정/삭제/파일 추가/파일 삭제
	 * - FIELD: 상태/우선순위/담당자/진척도/제목/설명/파일 등 변경항목
	 * - CHANGED_BY: 프로젝트 내 작업자
	 * - ISSUE_TYPE: 프로젝트에 연결된 일감 유형
	 * - STATUS: 일감 상태
	 * - PRIORITY: 우선순위 코드
	 *
	 * 핵심:
	 * - 최초 HTML 진입 때 실행하지 말고 /filter-options Ajax에서만 실행
	 * - 가능하면 프로젝트 단위 캐싱 후보
	 */
	List<WorkhistoryVO> selectFilterOptionRows(
			@Param("projectId") String projectId,
			@Param("userId") String userId);
}
