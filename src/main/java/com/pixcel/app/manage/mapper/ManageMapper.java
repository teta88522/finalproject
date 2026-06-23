package com.pixcel.app.manage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;

@Mapper
public interface ManageMapper {
    // 관리자가 생성한 프로젝트 목록
    public List<ManageProjectVO> selectMyManageProjectList(@Param("ownerId") String ownerId);

    // 관리자가 생성한 프로젝트들의 그룹 목록
    public List<ManageGroupVO> selectMyManageGroupList(@Param("ownerId") String ownerId);
}
