package com.pixcel.app.manage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.manage.service.ManageGroupMemberVO;
import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;
import com.pixcel.app.manage.service.ManageRoleVO;

@Mapper
public interface ManageMapper {
    // 관리자가 생성한 프로젝트 목록
    public List<ManageProjectVO> selectMyManageProjectList(@Param("ownerId") String ownerId);

    // 관리자가 생성한 프로젝트들의 그룹 목록
    public List<ManageGroupVO> selectMyManageGroupList(@Param("ownerId") String ownerId,
            @Param("projectIdList") List<String> projectIdList,
            @Param("groupName") String groupName,
            @Param("roleName") String roleName);
    
    // 관리자가 생성한 역할 목록
    public List<ManageRoleVO> selectMyManageRoleList(@Param("ownerId") String ownerId);
    
    // 프로젝트 소유자 검증
    public int selectProjectOwnerCheckCount(@Param("projectId") String projectId, 
                                            @Param("ownerId") String ownerId);
    
    // 그룹명 중복 확인
    public int selectManageGroupDuplicateCount(@Param("projectId") String projectId,
                                               @Param("groupName") String groupName);
    
    // 그룹 생성
    public int insertManageGroup(ManageGroupVO manageGroupVO);
    
    // 그룹 상세 조회
    public ManageGroupVO selectManageGroupDetail(@Param("projectGroupId") String projectGroupId,
                                                 @Param("ownerId") String ownerId);
    
    // 그룹 수정
    public int updateManageGroup(ManageGroupVO manageGroupVO);
    
    // 그룹 삭제
    public int deleteManageGroup(@Param("projectGroupId") String projectGroupId,
                                 @Param("ownerId") String ownerId);
    
    // 그룹 소속된 인원수 확인
    public int selectGroupMemberCount(@Param("projectGroupId") String projectGroupId);
    
    // 현재 그룹에 배정된 구성원 목록
    public List<ManageGroupMemberVO> selectAssignedGroupMemberList(@Param("projectGroupId") String projectGroupId,
                                                                   @Param("ownerId") String ownerId);

    // 그룹에 배정 가능한 구성원 목록
    public List<ManageGroupMemberVO> selectAssignableGroupMemberList(@Param("projectGroupId") String projectGroupId,
                                                                     @Param("ownerId") String ownerId);
    
    // 그룹 구성원 추가
    public int addGroupMember(@Param("projectGroupId") String projectGroupId,
                              @Param("projectMemberId") String projectMemberId,
                              @Param("ownerId") String ownerId);

    // 그룹 구성원 제외
    public int removeGroupMember(@Param("projectGroupId") String projectGroupId,
                                 @Param("projectMemberId") String projectMemberId,
                                 @Param("ownerId") String ownerId);
}