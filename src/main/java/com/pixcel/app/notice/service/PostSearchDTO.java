package com.pixcel.app.notice.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchDTO {
	private String startDate;
	private String endDate;
	private String searchType;
	private String keyword;
	private String sortBy; // "latest"(최신순, 기본) 또는 "views"(조회수순)
}
