package com.pixcel.app.file.mapper;

import java.util.List;import org.springframework.data.repository.query.Param;

import com.pixcel.app.file.service.FileVO;

public interface FileMapper {
	public List<FileVO> selectAll(String connectAddress);
	public int insertFile(FileVO fileVO);
	public int selectMaxVersion(
            @Param("projectId") String projectId,
            @Param("connectAddress") String connectAddress,
            @Param("originalName") String originalName);
}
