package com.pixcel.app.project.service;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectVO {
	private String projectId;
	private String ownerId;
	private String projectName;
	private String identifier;
	private String description;
	private String projectUrl;
	private String statusCode;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endDate;
	private Date createdAt;
	private String gitUrl;

	private List<String> moduleCodes;

	// 260623 고동현 프로젝트 리스트 화면 표시용으로 VO 추가합니다.
	// 화면 표시용
	private String statusName;
	private String ownerName;

	// 검색 조건 필드
	private String searchProjectName;
	private String searchOwnerName;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date searchStartDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date searchEndDate;
	private String searchStatusCode;

	// 페이징 필드
	// @Builder.Default: @Builder 사용 시에도 기본값 보장
	@Builder.Default
	private int page = 1;
	@Builder.Default
	private int pageSize = 10;
	private int offset;
	private int totalCount;

	public void calcOffset() {
		this.offset = (this.page - 1) * this.pageSize;
	}

	public int getTotalPage() {
		if (totalCount == 0) return 1;
		return (int) Math.ceil((double) totalCount / pageSize);
	}

	public boolean isHasPrevious() {
		return page > 1;
	}

	public boolean isHasNext() {
		return page < getTotalPage();
	}
}