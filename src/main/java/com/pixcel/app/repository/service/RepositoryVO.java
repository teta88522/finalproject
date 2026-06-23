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
	private String fileCode;
	private String originalName;
	private String storedName;
	private String filePath;
	
	private long fileSize;
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
	private Date uploadDate;
	private String fileVersion;
	private String fileUseYn;
	private String connectAddress;
}
