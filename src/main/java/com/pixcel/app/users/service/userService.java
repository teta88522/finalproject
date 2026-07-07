package com.pixcel.app.users.service;

import java.util.List;
import java.util.Map;

import com.pixcel.app.project.service.ProjectVO;

public interface userService {

	//회원가입
	public Map<String, Object> joinUser(userServiceVO userVO);
	
	//아이디 중복확인
	public boolean checkLoginId(String loginId);
	
	public boolean checkPhone(String phone);
	
    // 이메일 중복확인
    public boolean checkEmail(String email);

	//김덕모 마이페이지 설정
	public userServiceVO getUserDetail(String userId);
	public List<ProjectVO> selectMyProjectList(String userId);
	public Map<String, Object> updateUser(userServiceVO userVO);
	public Map<String, Object> updatePassword(String userId, String currentPassword, String newPassword);
	public void updateSubscribeStatus(String email);
	public List<ProjectVO> getFilteredProjects(Map<String, Object> params);
	public void unsubscribeUser(String userId);
}
