package com.pixcel.app.sourcerepository.service;

import java.util.List;
import java.util.Map;

public interface sourcerepositoryService {
	
	// ====== 파일 관리 ======
	
	// 1. GitHub에서 파일/폴더 목록 조회
	public List<Map<String, Object>> getRepositoryFiles(String projectId, String gitUrl, String path, String branch);

	// 2. GitHub에서 파일 내용 조회
	public String getFileContent(String projectId, String filePath, String branch);

	// 3. GitHub URL 저장 (PROJECT 테이블 업데이트)
	public void saveGitHubUrl(String projectId, String gitHubUrl);

	
	// ====== Commit 관리 ======
	
	// 4. GitHub에서 Commit 조회 & DB 동기화
	public void syncSourcerepositoryFromGitHub(String projectId, String branch);

	// 5. DB에서 Commit 조회 (페이지네이션)
	public sourcerepositoryPageVO<sourcerepositoryVO> getSourcerepositoryCommits(String projectId, 
																				 String branch, 
																				 int page, 
																				 int pageSize);

	// 6. Commit 상세 조회 (GitHub API)
	public sourcerepositoryVO getSourcerepositoryCommitDetail(String commitHash);

	// 7. 단일 Commit 조회
	public sourcerepositoryVO getSourcerepositoryCommit(String commitHash);

	// 8. 프로젝트별 모든 Commit 조회
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByProjectId(String projectId);

	// 9. 이슈별 모든 Commit 조회
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByIssueId(String issueId);

	// 10. Commit 삭제
	public void deleteSourcerepositoryCommit(String commitId);

	// 11. 브랜치별 Commit 삭제
	public void deleteSourcerepositoryCommitsByBranch(String projectId, String branchName);
	
	// 4. GitHub URL 검증 & 브랜치 목록 조회
	public Map<String, Object> getGitHubBranches(String gitHubUrl);
}