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

	// 5. SHA(+branch)로 특정 Commit 조회
	// ✅ 같은 SHA가 여러 브랜치에 걸쳐 존재할 수 있어(브랜치별 dedup 수정 이후) branchName으로 좁힘.
	//    branchName이 없으면 가장 최근 커밋 1건을 반환.
	public sourcerepositoryVO selectSourcerepositoryCommitBySha(@Param("commitHash") String commitHash,
			@Param("branchName") String branchName);

	// 6. 중복 체크 (같은 SHA가 이미 있는지)
	public int existsSourcerepositoryCommitBySha(@Param("commitHash") String commitHash);

	// ✅ 6-1. 중복 체크 (같은 프로젝트+브랜치 내에 같은 SHA가 있는지) - 브랜치별 동기화 시 사용
	public int existsSourcerepositoryCommitByShaAndBranch(@Param("commitHash") String commitHash,
			@Param("projectId") String projectId, @Param("branchName") String branchName);

	// ✅ 6-2. 동기화 전 "이미 저장된 SHA 목록"을 한 번에 조회 (commit마다 개별 SELECT 하던 걸 1번으로 줄이기 위함)
	public List<String> selectExistingCommitHashesByProjectAndBranch(@Param("projectId") String projectId,
			@Param("branchName") String branchName);

	// 7. 단일 Commit 삭제
	public int deleteSourcerepositoryCommit(@Param("commitId") String commitId);

	// 8. 브랜치별 모든 Commit 삭제
	public int deleteSourcerepositoryCommitsByBranch(@Param("projectId") String projectId, @Param("branchName") String branchName);

	// 9. 프로젝트별 모든 Commit 조회
	public List<sourcerepositoryVO> selectSourcerepositoryCommitsByProjectId(@Param("projectId") String projectId);

	// 10. 이슈별 모든 Commit 조회
	public List<sourcerepositoryVO> selectSourcerepositoryCommitsByIssueId(@Param("issueId") String issueId);
	
	// DB에서 프로젝트의 모든 BRANCH_NAME 조회 (중복 제거)
	public String selectFirstBranchNameByProjectId(@Param("projectId") String projectId);
}