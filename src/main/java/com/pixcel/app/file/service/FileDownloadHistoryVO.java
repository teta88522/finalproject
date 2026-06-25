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
public class FileDownloadHistoryVO {
	private String downloadHistoryId;  // PK
	private String fileId;
	private String downloadUserId; // FK
//	@DateTimeFormat(pattern="yyyy-MM-dd")
//	private String uploadDate;

}
