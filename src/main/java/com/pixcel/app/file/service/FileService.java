package com.pixcel.app.file.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface FileService {
	public void downloadAll(String connectAddress, HttpServletResponse response, String userId, Integer documentVersionId) throws IOException;;
	public void downloadOne(String fileId, HttpServletResponse response, String userId) throws IOException;
	public List<FileVO> selectAll(String connectAddress, Integer documentVersionId);
	public int uploadFile(List<MultipartFile> files, FileDTO req);
	public List<FileVO> selectByDocumentVersion(int documentVersionId);
	void copyOldFiles(int oldDocumentVersionId, int newDocumentVersionId, List<MultipartFile> uploadFiles);
	public List<FileDownloadHistoryVO> selectDownloadHistory(@Param("connectAddress") String connectAddress,@Param("documentVersionId") int documentVersionId);
}
