package com.pixcel.app.users.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.users.mapper.userMapper;
import com.pixcel.app.users.service.userService;
import com.pixcel.app.users.service.userServiceVO;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class userServiceImpl implements userService {
	
	public final userMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	//회원가입
	@Override
	@Transactional
	public Map<String, Object> joinUser(userServiceVO userVO) {
		Map<String, Object> resultMap = new HashMap<>();
		
		//회원가입 시 한번더 아이디 중복 체크를 진행한다.
		int count = userMapper.checkLoginId(userVO.getLoginId());
		if(count > 0) {
			resultMap.put("result",false);
			resultMap.put("message","이미 사용중인 아이디입니다.");
			return resultMap;
		}
		
		int phoneCount = userMapper.checkPhone(userVO.getPhone());
		if(phoneCount > 0) {
			resultMap.put("result", false);
			resultMap.put("message", "이미 등록된 연락처입니다.");
			return resultMap;
		}
		
		// 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(userVO.getPassword());
		userVO.setPassword(encodedPassword);
		
		//Insert
		int result = userMapper.insertUser(userVO);
		
		if(result > 0) {
			resultMap.put("result",true);
			resultMap.put("message", "회원가입이 완료되었습니다.");
			resultMap.put("userId", userVO.getUserId());
		}else {
			resultMap.put("result", false);
			resultMap.put("message", "회원가입이 실패하였습니다.");
		}
		return resultMap;
	}

	//아이디 중복확인
	@Override
	public boolean checkLoginId(String loginId) {
		int count = userMapper.checkLoginId(loginId);
		return count > 0;
	}

	//김덕모 마이페이지
	@Override
	public userServiceVO getUserDetail(String userId) {
		return userMapper.getUserDetail(userId);
	}
	@Override
	public List<ProjectVO> selectMyProjectList(String userId){
		return userMapper.selectMyProjectList(userId);
	}
	@Override
	@Transactional
	public Map<String, Object> updateUser(userServiceVO userVO){
		Map<String, Object> updateMap = new HashMap<>();

		int update = userMapper.updateUser(userVO);

		if(update > 0){
			updateMap.put("update",true);
			updateMap.put("message","프로필 정보가 성공적으로 수정되었습니다");
		} else {
			updateMap.put("update", false);
			updateMap.put("message", "프로필 정보 수정에 실패했습니다. 다시 시도해 주세요.");
		}

		return updateMap;
	}
	@Override
	@Transactional
	public Map<String, Object> updatePassword(String userId, String currentPassword, String newPassword){
		Map<String, Object> updatePasswordMap = new HashMap<>();

		userServiceVO user = userMapper.getUserDetail(userId);

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
    		updatePasswordMap.put("result", false);
   			updatePasswordMap.put("message", "현재 비밀번호가 일치하지 않습니다.");
    		return updatePasswordMap; 
		}
		String encodedPassword = passwordEncoder.encode(newPassword);	

		int update = userMapper.updatePassword(userId, encodedPassword);

		if (update > 0) {
   			updatePasswordMap.put("result", true);
    		updatePasswordMap.put("message", "비밀번호가 성공적으로 변경되었습니다.");
		} else {
    		updatePasswordMap.put("result", false);
    		updatePasswordMap.put("message", "비밀번호 변경에 실패했습니다. 다시 시도해 주세요.");
		}
		
		return updatePasswordMap;

	}
	
	@Override
	public void updateSubscribeStatus(String email) {
		int result = userMapper.updateSubscribeStatus(email);
		
		if(result ==0) {
			throw new RuntimeException("해당 이메일 [" + email + "]로 가입된 회원 정보를 찾을 수 없습니다.");
		}
	}
	
	@Override
    public List<ProjectVO> getFilteredProjects(Map<String, Object> params) {
        // 여기서 추가적인 비즈니스 로직(데이터 가공 등)이 필요하면 작성합니다.
        return userMapper.getFilteredProjects(params);
    }
	
	@Override
	public void unsubscribeUser(String userId) {
		userMapper.updateUnsubscribeStatus(userId);
	}

	@Override
	public boolean checkPhone(String phone) {
		int count = userMapper.checkPhone(phone);
		return count > 0;
	}


}
