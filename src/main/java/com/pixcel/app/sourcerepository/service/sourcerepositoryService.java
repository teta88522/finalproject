package com.pixcel.app.sourcerepository.service;

import java.util.List;

public interface sourcerepositoryService {
	
	// 1. GitHub에서 Commit 조회 & DB 동기화
		public void syncSourcerepositoryFromGitHub(String projectId, String branch);

		// 2. DB에서 Commit 조회 (페이지네이션)
		public sourcerepositoryPageVO<sourcerepositoryVO> getSourcerepositoryCommits(String projectId, 
																					 String branch, 
																					 int page, 
																					 int pageSize);

		// 3. Commit 상세 조회 (GitHub API)
		public sourcerepositoryVO getSourcerepositoryCommitDetail(String commitHash);

		// 4. 단일 Commit 조회
		public sourcerepositoryVO getSourcerepositoryCommit(String commitHash);

		// 5. 프로젝트별 모든 Commit 조회
		public List<sourcerepositoryVO> getSourcerepositoryCommitsByProjectId(String projectId);

		// 6. 이슈별 모든 Commit 조회
		public List<sourcerepositoryVO> getSourcerepositoryCommitsByIssueId(String issueId);

		// 7. Commit 삭제
		public void deleteSourcerepositoryCommit(String commitId);

		// 8. 브랜치별 Commit 삭제
		public void deleteSourcerepositoryCommitsByBranch(String projectId, String branchName);
}
