package com.pixcel.app.commonfile.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonFileUploadDTO {

    private String projectId;
    private String versionId;
    private String fileCode;
    private String uploadUserId;
    private String connectAddress;
}
