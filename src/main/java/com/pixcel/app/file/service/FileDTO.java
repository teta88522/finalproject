package com.pixcel.app.file.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
	private String projectId; // FK
	private String versionId; // FK
	private String fileCode;
	private String uploadUserId;
	private String connectAddress;
}
