package com.pixcel.app.file.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface FileService {
	public void downloadAll(String connectAddress, HttpServletResponse response, String userId) throws IOException;;
	public void downloadOne(String fileId, HttpServletResponse response, String userId) throws IOException;
	public List<FileVO> selectAll(String connectAddress);
	public int uploadFile(List<MultipartFile> files, FileDTO req);
}
