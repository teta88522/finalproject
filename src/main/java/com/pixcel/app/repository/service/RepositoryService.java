package com.pixcel.app.repository.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface RepositoryService {
	// 1. 자료실 전체 조회
	public List<RepositoryVO> getRepositoryList(RepositoryVO searchVO);

	// 2. 자료실 등록한 사람의 자료 상세조회
	public List<RepositoryVO> getRepositoryListByUserId(String uploadUserId);

	// 3. 자료실 상세조회
	public RepositoryVO getRepositoryDetail(String fileId);

	// 4. 자료실 파일 등록
	public int registerRepository(RepositoryVO repositoryVO, MultipartFile uploadFile);

	// 5. 자료실 파일 수정
	public int modifyRepository(RepositoryVO repositoryVO, MultipartFile uploadFile);

	// 6. 자료실 파일 삭제
	public int removeRepository(String fileId, String userId);
	
	// 7. 
	public int getTotalCount(RepositoryVO searchVO);
}
