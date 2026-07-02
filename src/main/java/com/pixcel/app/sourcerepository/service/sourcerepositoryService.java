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

	// ✅ 4-1. 저장소의 모든 브랜치를 한 번에 동기화 (브랜치마다 syncSourcerepositoryFromGitHub를 순회 호출)
	//    반환값: {"succeeded": [성공한 브랜치명 목록], "failed": [실패한 브랜치명(+사유) 목록], "totalBranches": 전체 브랜치 수}
	public Map<String, Object> syncAllBranchesFromGitHub(String projectId);

	// 5. DB에서 Commit 조회 (페이지네이션)
	public sourcerepositoryPageVO<sourcerepositoryVO> getSourcerepositoryCommits(String projectId, 
																				 String branch, 
																				 int page, 
																				 int pageSize);

	// 6. Commit 상세 조회 (GitHub API)
	public sourcerepositoryVO getSourcerepositoryCommitDetail(String commitHash);

	// 7. 단일 Commit 조회
	// ✅ 같은 SHA가 여러 브랜치에 걸쳐 있을 수 있어 branchName으로 좁힘 (null이면 최신 1건)
	public sourcerepositoryVO getSourcerepositoryCommit(String commitHash, String branchName);

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
	
	// DB에서 프로젝트의 첫 번째 branch 조회
	public String getFirstBranchByProjectId(String projectId);
}