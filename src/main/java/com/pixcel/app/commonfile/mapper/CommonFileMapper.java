package com.pixcel.app.commonfile.mapper;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.commonfile.service.CommonFileVO;

public interface CommonFileMapper {

    int selectMaxFileVersion(
            @Param("projectId") String projectId,
            @Param("connectAddress") String connectAddress,
            @Param("originalName") String originalName
    );

    int insertFile(CommonFileVO commonFileVO);
}
