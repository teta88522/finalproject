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
	private Integer settingCodeId;
	private String settingGroupName;
	private String settingName;
	private String defaultYn;
	private String useYn;
	private String userId;
}