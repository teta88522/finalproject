package com.pixcel.app.repository.service;

import java.util.Date;

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
public class RepositoryVO {
	private String fileId;
	private String projectId;
	private String versionId;
	@Builder.Default
	private String fileCode = "f009";
	private String originalName;
	private String storedName;
	private String filePath;

	private long fileSize;
	
	private String categoryName;

	public String getFileSizeText() {
		if (fileSize == 0) {
			return "0 B";
		}

		if (fileSize >= 1024L * 1024L) {
			return String.format("%.2f MB", fileSize / 1024.0 / 1024.0);
		}

		if (fileSize >= 1024L) {
			return String.format("%.2f KB", fileSize / 1024.0);
		}

		return fileSize + " B";
	}

	private String uploadUserId;
	private String userName;
	private Date uploadDate;
	@Builder.Default
	private String fileVersion = "1";
	@Builder.Default
	private String fileUseYn = "g001";
	@Builder.Default
	private String connectAddress = "-";
	
	@Builder.Default		// page라는 필드를 사용할 때 어떻게 할지 명확하게 해주는 어노테이션.
	private int page = 1;	// 현재 페이지
	private int startRow;	// 시작 행
	private int endRow;		// 끝 행
	
	private int totalPage;
	private int startPage;
	private int endPage;
	private boolean hasPrevious;
	private boolean hasNext;
	
	private boolean first;
	
	public boolean isFirst() {
	    return first;
	}

	public void setFirst(boolean first) {
	    this.first = first;
	}
	
	private String startDate;
	private String endDate;
}
