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
    public List<ManageProjectVO> selectMyManageProjectList(@Param("ownerId") String ownerId);

    public List<ManageGroupVO> selectMyManageGroupList(@Param("ownerId") String ownerId,
            @Param("projectIdList") List<String> projectIdList,
            @Param("groupName") String groupName,
            @Param("roleName") String roleName);
    
    public List<ManageRoleVO> selectMyManageRoleList(@Param("ownerId") String ownerId);
    
    public int selectProjectOwnerCheckCount(@Param("projectId") String projectId, 
                                            @Param("ownerId") String ownerId);
    
    public int selectManageGroupDuplicateCount(@Param("projectId") String projectId,
                                               @Param("groupName") String groupName);
    
    public int insertManageGroup(ManageGroupVO manageGroupVO);
    
    public ManageGroupVO selectManageGroupDetail(@Param("projectGroupId") String projectGroupId,
                                                 @Param("ownerId") String ownerId);
    
    public int updateManageGroup(ManageGroupVO manageGroupVO);
    
    public int deleteManageGroup(@Param("projectGroupId") String projectGroupId,
                                 @Param("ownerId") String ownerId);
    
    public int selectGroupMemberCount(@Param("projectGroupId") String projectGroupId);
    
    public List<ManageGroupMemberVO> selectAssignedGroupMemberList(@Param("projectGroupId") String projectGroupId,
                                                                   @Param("ownerId") String ownerId);

    public List<ManageGroupMemberVO> selectAssignableGroupMemberList(@Param("projectGroupId") String projectGroupId,
                                                                     @Param("ownerId") String ownerId);
    
    public int addGroupMember(@Param("projectGroupId") String projectGroupId,
                              @Param("projectMemberId") String projectMemberId,
                              @Param("roleId") String roleId,
                              @Param("ownerId") String ownerId);

    public int removeGroupMember(@Param("projectGroupId") String projectGroupId,
                                 @Param("projectMemberId") String projectMemberId,
                                 @Param("ownerId") String ownerId);
}