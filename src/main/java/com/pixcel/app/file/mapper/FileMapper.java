package com.pixcel.app.file.mapper;

import java.util.List;

import org.springframework.data.repository.query.Param;


import com.pixcel.app.file.service.FileDownloadHistoryVO;
import com.pixcel.app.file.service.FileVO;

public interface FileMapper {
	public List<FileVO> downloadAll(String connectAddress, Integer documentVersionId);
	public FileVO downloadOne(String fileId);
	public int insertFile(FileVO fileVO);
	public int insertDownloadHistory(FileDownloadHistoryVO fileDownloadHistoryVO);
	public int selectMaxVersion(
            @Param("projectId") String projectId,
            @Param("connectAddress") String connectAddress,
            @Param("originalName") String originalName);
	public int selectNextFileVersion( @Param("projectId") String projectId,
            @Param("connectAddress") String connectAddress,
            @Param("originalName") String originalName);
	
	public List<FileVO> selectByDocumentVersion(int documentVersionId);
	public int copyFile(FileVO fileVO);
}
