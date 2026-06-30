package com.pixcel.app.sourcerepository.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Value("${github.api-url:https://api.github.com}")
	private String githubApiUrl;

	private static final Gson gson = new Gson();

	/**
	 * 1. GitHub에서 Commit 조회 & DB 동기화
	 */
	@Override
	public void syncSourcerepositoryFromGitHub(String projectId, String branch) {
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
			String url = String.format("%s/repos/%s/%s/commits?sha=%s&per_page=30", githubApiUrl, githubOwner,
					githubRepo, branch);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/vnd.github.v3+json");

			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			// 4. JSON 파싱
			JsonArray commits = gson.fromJson(response.getBody(), JsonArray.class);
			List<sourcerepositoryVO> commitList = new ArrayList<>();

			for (JsonElement element : commits) {
				JsonObject commitObj = element.getAsJsonObject();
				String commitHash = commitObj.get("sha").getAsString();

				// 중복 체크
				if (mapper.existsSourcerepositoryCommitBySha(commitHash) > 0) {
					continue;
				}

				// GitHub 응답에서 필요한 데이터 추출
				JsonObject commitData = commitObj.getAsJsonObject("commit");
				String message = commitData.get("message").getAsString();
				String authorName = commitData.getAsJsonObject("author").get("name").getAsString();
				String authorEmail = commitData.getAsJsonObject("author").get("email").getAsString();
				String committedAtStr = commitData.getAsJsonObject("committer").get("date").getAsString();

				// Commit 메시지에서 issueId 파싱
				String issueId = parseIssueIdFromMessage(message);

				// VO 생성
				sourcerepositoryVO commitLog = sourcerepositoryVO.builder().commitId(UUID.randomUUID().toString())
						.projectId(projectId).issueId(issueId).branchName(branch).commitHash(commitHash)
						.commitMessage(message).authorName(authorName).authorEmail(authorEmail)
						.committedAt(parseGitHubDate(committedAtStr)).createdAt(LocalDateTime.now()).build();

				commitList.add(commitLog);
			}

			// 5. DB 저장 (배치)
			if (!commitList.isEmpty()) {
				mapper.insertSourcerepositoryCommits(commitList);
			}

		} catch (Exception e) {
			throw new RuntimeException("GitHub 동기화 실패: " + e.getMessage());
		}
	}

	/**
	 * 2. DB에서 Commit 조회 (페이지네이션)
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
	 * 3. Commit 상세 조회 (DB에서 조회)
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
	 * 4. 단일 Commit 조회
	 */
	@Override
	public sourcerepositoryVO getSourcerepositoryCommit(String commitHash) {
		return mapper.selectSourcerepositoryCommitBySha(commitHash);
	}

	/**
	 * 5. 프로젝트별 모든 Commit 조회
	 */
	@Override
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByProjectId(String projectId) {
		return mapper.selectSourcerepositoryCommitsByProjectId(projectId);
	}

	/**
	 * 6. 이슈별 모든 Commit 조회
	 */
	@Override
	public List<sourcerepositoryVO> getSourcerepositoryCommitsByIssueId(String issueId) {
		return mapper.selectSourcerepositoryCommitsByIssueId(issueId);
	}

	/**
	 * 7. Commit 삭제
	 */
	@Override
	public void deleteSourcerepositoryCommit(String commitId) {
		mapper.deleteSourcerepositoryCommit(commitId);
	}

	/**
	 * 8. 브랜치별 Commit 삭제
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

	/**
	 * Commit 메시지에서 issueId 파싱 예: "Fix #123 - Update login" → "123"
	 */
	private String parseIssueIdFromMessage(String message) {
		try {
			Pattern pattern = Pattern.compile("#(\\d+)");
			Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				return matcher.group(1);
			}
		} catch (Exception e) {
			// 파싱 실패 시 null 반환
		}
		return null;
	}

	/**
	 * GitHub 날짜 형식 파싱 ISO 8601 형식 → LocalDateTime
	 */
	private LocalDateTime parseGitHubDate(String githubDate) {
		try {
			return LocalDateTime.parse(githubDate.replace("Z", "+00:00"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		} catch (Exception e) {
			return LocalDateTime.now();
		}
	}

}
