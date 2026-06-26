package com.pixcel.app.testcase.service;

import lombok.Data;

@Data
public class TestCaseSearchVO {
	
	private String projectId;
	private String versionId;
	
	private String testTypeCode;
	private String statusCode;
	private String priorityCode;
	private String title;
	
	//페이징
	private int page = 1;
	private int size = 10;
	private int startRow;
	private int endRow;
	
	private int totalCount;
	private int totalPage;
	
	public void calculatePagin() {
		
		if(page < 1) {
			page = 1;
		}
		if(size < 1) {
			size = 10;
		}
		
		startRow = ((page - 1) * size) + 1;
		endRow = page * size;
		
		if(totalCount > 0 ) {
			totalPage = (int)Math.ceil( (double) totalCount / size);
		}else {
			totalPage = 1;
		}
	}
	
}
