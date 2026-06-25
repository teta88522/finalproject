package com.pixcel.app.commonfile.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.commonfile.mapper.CommonFileMapper;
import com.pixcel.app.commonfile.service.CommonFileService;
import com.pixcel.app.commonfile.service.CommonFileUploadDTO;
import com.pixcel.app.commonfile.service.CommonFileVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonFileServiceImpl implements CommonFileService {

    private final CommonFileMapper commonFileMapper;

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public int uploadFiles(List<MultipartFile> files, CommonFileUploadDTO uploadDTO) {
        if (files == null || files.isEmpty() || uploadDTO == null) {
            return 0;
        }

        File uploadDir = new File(fileDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        int count = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try {
                String originalName = file.getOriginalFilename();
                if (!StringUtils.hasText(originalName)) {
                    continue;
                }

                originalName = StringUtils.cleanPath(originalName);
                if (originalName.contains("..")) {
                    continue;
                }

                int nextVersion = commonFileMapper.selectMaxFileVersion(
                        uploadDTO.getProjectId(),
                        uploadDTO.getConnectAddress(),
                        originalName
                ) + 1;

                String storedName = UUID.randomUUID() + "_" + originalName;
                File dest = new File(uploadDir, storedName);
                file.transferTo(dest);

                CommonFileVO fileVO = new CommonFileVO();
                fileVO.setProjectId(uploadDTO.getProjectId());
                fileVO.setVersionId(uploadDTO.getVersionId());
                fileVO.setFileCode(getFileCode(uploadDTO));
                fileVO.setOriginalName(originalName);
                fileVO.setStoredName(storedName);
                fileVO.setFilePath(dest.getAbsolutePath());
                fileVO.setFileSize(String.valueOf(file.getSize()));
                fileVO.setUploadUserId(uploadDTO.getUploadUserId());
                fileVO.setFileVersion(nextVersion);
                fileVO.setConnectAddress(uploadDTO.getConnectAddress());

                commonFileMapper.insertFile(fileVO);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    private String getFileCode(CommonFileUploadDTO uploadDTO) {
        if (StringUtils.hasText(uploadDTO.getFileCode())) {
            return uploadDTO.getFileCode();
        }

        return ISSUE_FILE_CODE;
    }
}
