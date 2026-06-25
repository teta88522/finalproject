package com.pixcel.app.commonfile.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CommonFileVO {

    private String fileId;
    private String projectId;
    private String versionId;
    private String fileCode;
    private String originalName;
    private String storedName;
    private String filePath;
    private String fileSize;
    private String uploadUserId;
    private int fileVersion;
    private String connectAddress;
}
