package com.pixcel.app.sourcerepository.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixcel.app.project.mapper.ProjectMapper;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.project.service.ProjectVO;
import com.pixcel.app.sourcerepository.mapper.sourcerepositoryMapper;
import com.pixcel.app.sourcerepository.service.sourcerepositoryPageVO;
import com.pixcel.app.sourcerepository.service.sourcerepositoryService;
import com.pixcel.app.sourcerepository.service.sourcerepositoryVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class sourcerepositoryServiceImpl implements sourcerepositoryService {

	private final sourcerepositoryMapper mapper;
	private final RestTemplate restTemplate;
	private final ProjectService projectService;
	private final ProjectMapper projectMapper;

	@Value("${github.api-url:https://api.github.com}")
	private String githubApiUrl;

	private static final Gson gson = new Gson();

	/**
	 * 1. GitHub에서 파일/폴더 목록 조회
	 */
	@Override
	public List<Map<String, Object>> getRepositoryFiles(String projectId, String gitUrl,  String path, String branch) {
	    try {
	        String owner = parseGitHubOwner(gitUrl);
	        String repo = parseGitHubRepo(gitUrl);

	        if (owner == null || repo == null) {
	            throw new RuntimeException("GitHub URL 파싱 실패");
	        }

	        // ✅ path null 처리 개선
	        String pathParam = "";
	        if (path != null && !path.isEmpty() && !path.equals("null")) {
	            pathParam = "/" + path;
	        }

	        String url = String.format(
	            "%s/repos/%s/%s/contents%s?ref=%s",
	            githubApiUrl, owner, repo, pathParam, branch
	        );

	        String response = restTemplate.getForObject(url, String.class);
	        JsonArray contents = JsonParser.parseString(response).getAsJsonArray();
	        List<Map<String, Object>> fileList = new ArrayList<>();

	        for (int i = 0; i < contents.size(); i++) {
	            JsonObject item = contents.get(i).getAsJsonObject();
	            Map<String, Object> fileInfo = new HashMap<>();
	            fileInfo.put("name", item.get("name").getAsString());
	            fileInfo.put("type", item.get("type").getAsString());
	            fileInfo.put("path", item.get("path").getAsString());
	            fileInfo.put("size", item.has("size") ? item.get("size").getAsInt() : 0);
	            fileInfo.put("url", item.get("html_url").getAsString());
	            fileList.add(fileInfo);
	        }

	        return fileList;

	    } catch (Exception e) {
	        throw new RuntimeException("파일 목록 조회 실패: " + e.getMessage(), e);
	    }
	}

	/**
	 * 2. GitHub에서 파일 내용 조회
	 */
	@Override
	public String getFileContent(String projectId, String filePath, String branch) {
		try {
			// 1. DB에서 프로젝트 정보 조회 (GIT_URL 포함)
			ProjectVO project = projectService.selectProjectDetail(projectId);
			String gitUrl = project.getGitUrl();

			if (gitUrl == null || gitUrl.isEmpty()) {
				throw new RuntimeException("GitHub URL이 설정되지 않았습니다");
			}

			// 2. GIT_URL에서 owner, repo 파싱
			String githubOwner = parseGitHubOwner(gitUrl);
			String githubRepo = parseGitHubRepo(gitUrl);

			// 3. GitHub API 호출
			String url = String.format("%s/repos/%s/%s/contents/%s?ref=%s", githubApiUrl, githubOwner, githubRepo,
					filePath, branch);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github.v3+json");

			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			// 4. JSON 파싱
			JsonObject fileObj = gson.fromJson(response.getBody(), JsonObject.class);

			// 5. content가 base64 인코딩되어 있으면 디코딩
			String encodedContent = fileObj.get("content").getAsString();
			String decodedContent = new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);

			return decodedContent;

		} catch (Exception e) {
			throw new RuntimeException("파일 내용 조회 실패: " + e.getMessage());
		}
	}
	
	

	/**
	 * 3. GitHub URL 저장 (PROJECT 테이블 업데이트)
	 */
	@Override
	public void saveGitHubUrl(String projectId, String gitHubUrl) {
		try {
			projectMapper.updateProjectGitUrl(projectId, gitHubUrl);
			System.out.println("✅ GitHub URL 저장 완료: " + gitHubUrl);
		} catch (Exception e) {
			throw new RuntimeException("GitHub URL 저장 실패: " + e.getMessage());
		}
	}

	@Override
	public Map<String, Object> getGitHubBranches(String gitHubUrl) {
		try {
			String githubOwner = parseGitHubOwner(gitHubUrl);
			String githubRepo = parseGitHubRepo(gitHubUrl);

			String repoUrl = String.format("%s/repos/%s/%s", githubApiUrl, githubOwner, githubRepo);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github.v3+json");

			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> repoResponse = restTemplate.exchange(repoUrl, HttpMethod.GET, entity, String.class);

			JsonObject repoObj = gson.fromJson(repoResponse.getBody(), JsonObject.class);
			String defaultBranch = repoObj.get("default_branch").getAsString();

			String branchUrl = String.format("%s/repos/%s/%s/branches?per_page=50", githubApiUrl, githubOwner,
					githubRepo);

			ResponseEntity<String> branchResponse = restTemplate.exchange(branchUrl, HttpMethod.GET, entity,
					String.class);
			JsonArray branches = gson.fromJson(branchResponse.getBody(), JsonArray.class);

			List<Map<String, String>> branchList = new ArrayList<>();
			for (JsonElement element : branches) {
				JsonObject branchObj = element.getAsJsonObject();
				Map<String, String> branch = new HashMap<>();
				branch.put("name", branchObj.get("name").getAsString());
				branch.put("isDefault", branchObj.get("name").getAsString().equals(defaultBranch) ? "true" : "false");
				branchList.add(branch);
			}

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("defaultBranch", defaultBranch);
			result.put("branches", branchList);
			result.put("owner", githubOwner);
			result.put("repo", githubRepo);

			return result;

		} catch (Exception e) {
			throw new RuntimeException("GitHub 저장소 검증 실패: " + e.getMessage());
		}
	}

	/**
	 * 4. GitHub에서 Commit 조회 & DB 동기화
	 */
	@Override
	public void syncSourcerepositoryFromGitHub(String projectId, String branch) {
	    try {
	        ProjectVO project = projectService.selectProjectDetail(projectId);
	        if (project == null || project.getGitUrl() == null) {
	            throw new RuntimeException("GitHub URL이 설정되지 않았습니다.");
	        }

	        // GitHub에서 Commit 목록 조회
	        List<sourcerepositoryVO> commits = fetchCommitsFromGitHub(
	            project.getGitUrl(), 
	            branch
	        );

	        // ✨ 신규 Commit 필터링
	        List<sourcerepositoryVO> newCommits = new ArrayList<>();
	        for (sourcerepositoryVO commit : commits) {
	        	int existsCount = mapper.existsSourcerepositoryCommitBySha(commit.getCommitHash());
	        	boolean exists = existsCount > 0;
	            if (!exists) {
	                commit.setProjectId(projectId);
	                commit.setBranchName(branch);
	                commit.setCommitId(UUID.randomUUID().toString());
	                commit.setCreatedAt(LocalDateTime.now());
	                newCommits.add(commit);
	            }
	        }

	        // ✨ 신규 Commit 개별 저장
	        if (!newCommits.isEmpty()) {
	            for (sourcerepositoryVO commit : newCommits) {
	                try {
	                    mapper.insertSourcerepositoryCommit(commit);
	                    System.out.println("✅ Commit 저장: " + commit.getCommitHash());
	                } catch (Exception e) {
	                    System.err.println("❌ Commit 저장 실패: " + commit.getCommitHash() + " - " + e.getMessage());
	                }
	            }
	        }

	        System.out.println("✅ GitHub 동기화 완료: " + newCommits.size() + "개 Commit 저장됨");

	    } catch (Exception e) {
	        throw new RuntimeException("GitHub 동기화 실패: " + e.getMessage(), e);
	    }
	}
	
	private List<sourcerepositoryVO> fetchCommitsFromGitHub(String gitUrl, String branch) {
	    try {
	        String owner = parseGitHubOwner(gitUrl);
	        String repo = parseGitHubRepo(gitUrl);

	        if (owner == null || repo == null) {
	            throw new RuntimeException("GitHub URL 파싱 실패");
	        }

	        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo +
	                       "/commits?sha=" + branch + "&per_page=30";

	        String response = restTemplate.getForObject(apiUrl, String.class);
	        JsonArray commits = JsonParser.parseString(response).getAsJsonArray();
	        List<sourcerepositoryVO> commitList = new ArrayList<>();

	        for (int i = 0; i < commits.size(); i++) {
	            JsonObject commit = commits.get(i).getAsJsonObject();

	            sourcerepositoryVO vo = new sourcerepositoryVO();
	            vo.setCommitHash(commit.get("sha").getAsString());
	            vo.setCommitMessage(commit.getAsJsonObject("commit")
	                .get("message").getAsString());
	            vo.setAuthorName(commit.getAsJsonObject("commit")
	                .getAsJsonObject("author").get("name").getAsString());
	            vo.setAuthorEmail(commit.getAsJsonObject("commit")
	                .getAsJsonObject("author").get("email").getAsString());

	            String committedAt = commit.getAsJsonObject("commit")
	                .getAsJsonObject("author").get("date").getAsString();
	            vo.setCommittedAt(LocalDateTime.parse(
	                committedAt.replace("Z", "+00:00"),
	                java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
	            ).withNano(0));

	            // ❌ 이 줄 제거!
	            // vo.setIssueId("-");

	            commitList.add(vo);
	        }

	        return commitList;

	    } catch (Exception e) {
	        throw new RuntimeException("GitHub Commit 조회 실패: " + e.getMessage(), e);
	    }
	}

	/**
	 * 5. DB에서 Commit 조회 (페이지네이션)
	 */
	@Override
	public sourcerepositoryPageVO<sourcerepositoryVO> getSourcerepositoryCommits(String projectId, String branch,
			int page, int pageSize) {
		try {
			// 1. 총 개수 조회
			int totalElements = mapper.countSourcerepositoryCommitsByProjectAndBranch(projectId, branch);

			// 2. offset 계산
			int offset = (page - 1) * pageSize;

			// 3. 데이터 조회
			List<sourcerepositoryVO> commits = mapper.selectSourcerepositoryCommitsByProjectAndBranch(projectId, branch,
					offset, pageSize);

			// 4. Page 객체 반환
			return new sourcerepositoryPageVO<>(commits, page, totalElements, pageSize);

		} catch (Exception e) {
			throw new RuntimeException("Commit 조회 실패: " + e.getMessage());
		}
	}

	/**
	 * 6. Commit 상세 조회 (DB에서 조회)
	 */
	@Override
	public sourcerepositoryVO getSourcerepositoryCommitDetail(String commitHash) {
		try {
			return mapper.selectSourcerepositoryCommitBySha(commitHash);

		} catch (Exception e) {
			throw new RuntimeException("Commit 상세 조회 실패: " + e.getMessage());
		}
	}

	/**
	 * 7. 단일 Commit 조회
	 */
	@Override
	public sourcerepositoryVO getSourcerepositoryCommit(String commitHash) {
		return mapper.selectSourcerepositoryCommitBySha(commitHash);
	}

	/**
	 * 8. 프로젝트별 모든 Commit 조회
	 */
	@Override
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByProjectId(String projectId) {
		return mapper.selectSourcerepositoryCommitsByProjectId(projectId);
	}

	/**
	 * 9. 이슈별 모든 Commit 조회
	 */
	@Override
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByIssueId(String issueId) {
		return mapper.selectSourcerepositoryCommitsByIssueId(issueId);
	}

	/**
	 * 10. Commit 삭제
	 */
	@Override
	public void deleteSourcerepositoryCommit(String commitId) {
		mapper.deleteSourcerepositoryCommit(commitId);
	}

	/**
	 * 11. 브랜치별 Commit 삭제
	 */
	@Override
	public void deleteSourcerepositoryCommitsByBranch(String projectId, String branchName) {
		mapper.deleteSourcerepositoryCommitsByBranch(projectId, branchName);
	}

	/**
	 * GIT_URL에서 GitHub Owner 추출 예: https://github.com/pixcel-team/pixcel-project →
	 * pixcel-team
	 */
	private String parseGitHubOwner(String gitUrl) {
		try {
			// .git 제거
			String cleanUrl = gitUrl.replaceAll("\\.git$", "");
			// / 기준으로 분할
			String[] parts = cleanUrl.split("/");
			// 뒤에서 두 번째가 owner
			return parts[parts.length - 2];
		} catch (Exception e) {
			throw new RuntimeException("GitHub Owner 파싱 실패: " + gitUrl);
		}
	}

	/**
	 * GIT_URL에서 GitHub Repo 추출 예: https://github.com/pixcel-team/pixcel-project →
	 * pixcel-project
	 */
	private String parseGitHubRepo(String gitUrl) {
		try {
			// .git 제거
			String cleanUrl = gitUrl.replaceAll("\\.git$", "");
			// / 기준으로 분할
			String[] parts = cleanUrl.split("/");
			// 마지막이 repo
			return parts[parts.length - 1];
		} catch (Exception e) {
			throw new RuntimeException("GitHub Repo 파싱 실패: " + gitUrl);
		}
	}
	
}