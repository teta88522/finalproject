package com.pixcel.app.commonfile.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CommonFileService {

    String ISSUE_FILE_CODE = "f008";

    int uploadFiles(List<MultipartFile> files, CommonFileUploadDTO uploadDTO);
}
