package com.pixcel.app.file.service;


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
public class FileVO {
	private String fileId;  // PK
	private String projectId; // FK
	private String versionId; // FK
	private String fileCode;
	private String originalName;
	private String storedName;
	private String filePath;
	private String fileSize;
	private String uploadUserId;
//	@DateTimeFormat(pattern="yyyy-MM-dd")
//	private String uploadDate;
	private int fileVersion;
//	private String fileUseYn;
	private String connectAddress;
}
