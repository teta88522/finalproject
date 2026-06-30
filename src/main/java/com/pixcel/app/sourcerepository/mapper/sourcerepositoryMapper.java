package com.pixcel.app.sourcerepository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.pixcel.app.sourcerepository.service.sourcerepositoryVO;

public interface sourcerepositoryMapper {
	// 1. 단일 Commit 저장
	public int insertSourcerepositoryCommit(sourcerepositoryVO commit);

	// 2. 여러 Commit 배치 저장
	public int insertSourcerepositoryCommits(@Param("commits") List<sourcerepositoryVO> commits);

	// 3. 프로젝트 + 브랜치별 Commit 조회 (페이지네이션)
	public List<sourcerepositoryVO> selectSourcerepositoryCommitsByProjectAndBranch(@Param("projectId") String projectId, 
																					@Param("branchName") String branchName, 
																					@Param("offset") int offset,
																					@Param("limit") int limit);

	// 4. 프로젝트 + 브랜치별 Commit 총 개수
	public int countSourcerepositoryCommitsByProjectAndBranch(@Param("projectId") String projectId, @Param("branchName") String branchName);

	// 5. SHA로 특정 Commit 조회
	public sourcerepositoryVO selectSourcerepositoryCommitBySha(@Param("commitHash") String commitHash);

	// 6. 중복 체크 (같은 SHA가 이미 있는지)
	public int existsSourcerepositoryCommitBySha(@Param("commitHash") String commitHash);

	// 7. 단일 Commit 삭제
	public int deleteSourcerepositoryCommit(@Param("commitId") String commitId);

	// 8. 브랜치별 모든 Commit 삭제
	public int deleteSourcerepositoryCommitsByBranch(@Param("projectId") String projectId, @Param("branchName") String branchName);

	// 9. 프로젝트별 모든 Commit 조회
	public List<sourcerepositoryVO> selectSourcerepositoryCommitsByProjectId(@Param("projectId") String projectId);

	// 10. 이슈별 모든 Commit 조회
	public List<sourcerepositoryVO> selectSourcerepositoryCommitsByIssueId(@Param("issueId") String issueId);
}
