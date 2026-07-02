package com.pixcel.app.users.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.users.service.userServiceVO;

@Mapper
public interface userMapper {
	//회원가입
	public int insertUser(userServiceVO userVO);
	//아이디 중복 확인(회원가입시)
	public int checkLoginId(String loginId);
	
	public int checkPhone(String phone);
	
	//로그인
	public userServiceVO selectUserByLoginId(String loginId);

	//김덕모 마이페이지 설정
	public userServiceVO getUserDetail(String userId);
	public List<ProjectVO> selectMyProjectList(@Param("userId") String userId);
	public int updateUser(userServiceVO userVO);
	public int updatePassword(@Param("userId") String userId,@Param("password")String password);
}
