package com.pixcel.app.codevalue.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CodeValueVO {

	// 설정코드 ID
	private String settingCodeId;

	// 관리자 ID
	private String userId;

	// 설정코드 그룹코드
	private String settingGroupName;

	// 설정 명
	private String settingName;

	// 기본값 여부
	private String defaultYn;

	// 사용 여부
	private String useYn;
}