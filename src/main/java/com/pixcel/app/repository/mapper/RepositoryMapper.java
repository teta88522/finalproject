package com.pixcel.app.repository.mapper;

import java.util.List;

import com.pixcel.app.repository.service.RepositoryVO;

public interface RepositoryMapper {
	// 1. 자료실 전체 조회
	public List<RepositoryVO> selectRepositoryList(RepositoryVO searchVO);

	// 2. 자료실 등록한 사람의 자료 상세조회
	public List<RepositoryVO> selectRepositoryListByUserId(String uploadUserId);

	// 3. 자료실 상세조회
	public RepositoryVO selectRepositoryDetail(String fileId);

	// 4. 자료실 파일 등록
	public int insertRepository(RepositoryVO repositoryVO);

	// 5. 자료실 파일 수정
	public int updateRepository(RepositoryVO repositoryVO);
	public int updateRepositoryUseYn(String fileId, String fileUseYn);

	// 6. 자료실 파일 삭제
	public int deleteRepository(String fileId);
	
	// 7. 페이징용
	public int selectRepositoryTotalCount(RepositoryVO searcVO);
}
