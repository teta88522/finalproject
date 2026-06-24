package com.pixcel.app.document.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class DocumentCategoryVO {
	private String documentCategoryId;
	private String projectId;
	private String categoryName;
	private int totalCnt;
}
