package com.pixcel.app.file.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	public List<FileVO> findAll(String connectAddress);
	public int uploadFile(List<MultipartFile> files, FileDTO req);
}
