package com.pixcel.app.project.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueStatVO {
	private String projectId;              // 프로젝트 ID
	private String issueTypeCode;          // 이슈 타입 코드 (h001, h002, ... 등)
	private String typeName;               // 이슈 타입명 (새기능, 결함, 요구사항, ... 등)
	private int inProgressCount;           // 진행중인 일감 개수
	private int doneCount;                 // 완료된 일감 개수
	private int totalCount;                // 전체 일감 개수
}
