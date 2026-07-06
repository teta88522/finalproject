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

	// ✅ GitHub Personal Access Token (application.yml: github.token)
	// 인증 없이 호출하면 IP당 60회/시간 제한 → 토큰 사용 시 5,000회/시간으로 상향됨
	@Value("${github.token:}")
	private String githubToken;

	private static final Gson gson = new Gson();

	// ✅ 동일 저장소에 대한 반복 호출을 줄이기 위한 초간단 캐시 (branches 조회 결과, 60초 TTL)
	private final Map<String, CachedBranchResult> branchCache = new java.util.concurrent.ConcurrentHashMap<>();
	private static final long BRANCH_CACHE_TTL_MILLIS = 60_000L;

	private static class CachedBranchResult {
		final Map<String, Object> result;
		final long cachedAt;

		CachedBranchResult(Map<String, Object> result) {
			this.result = result;
			this.cachedAt = System.currentTimeMillis();
		}

		boolean isExpired() {
			return System.currentTimeMillis() - cachedAt > BRANCH_CACHE_TTL_MILLIS;
		}
	}

	/**
	 * ✅ GitHub API 호출용 공통 헤더 생성 (토큰이 설정되어 있으면 Authorization 헤더 포함)
	 */
	private HttpHeaders buildGitHubHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/vnd.github.v3+json");
		if (githubToken != null && !githubToken.isEmpty()) {
			headers.set("Authorization", "Bearer " + githubToken);
		}
		return headers;
	}

	/**
	 * 1. GitHub에서 파일/폴더 목록 조회
	 */
	@Override
	public List<Map<String, Object>> getRepositoryFiles(String projectId, String gitUrl, String path, String branch) {
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

			String url = String.format("%s/repos/%s/%s/contents%s?ref=%s", githubApiUrl, owner, repo, pathParam,
					branch);

			// ✅ 인증 헤더 적용 (기존 getForObject는 헤더를 태울 수 없어 exchange로 변경)
			HttpEntity<String> entity = new HttpEntity<>(buildGitHubHeaders());
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			String response = responseEntity.getBody();
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

			// ✅ GitHub UI처럼 폴더(dir) 먼저, 그다음 파일(file), 각각 이름 알파벳순 정렬
			//    GitHub Contents API는 정렬을 보장하지 않으므로 서버에서 명시적으로 정렬
			fileList.sort((a, b) -> {
				String typeA = (String) a.get("type");
				String typeB = (String) b.get("type");
				boolean isDirA = "dir".equals(typeA);
				boolean isDirB = "dir".equals(typeB);
				if (isDirA != isDirB) {
					return isDirA ? -1 : 1; // 폴더가 파일보다 먼저
				}
				String nameA = (String) a.get("name");
				String nameB = (String) b.get("name");
				return nameA.compareToIgnoreCase(nameB);
			});

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

			// ✅ 인증 헤더 적용
			HttpEntity<String> entity = new HttpEntity<>(buildGitHubHeaders());
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			// 4. JSON 파싱
			JsonObject fileObj = gson.fromJson(response.getBody(), JsonObject.class);

			// 5. content가 base64 인코딩되어 있으면 디코딩
			String encodedContent = fileObj.get("content").getAsString();
			// ✅ GitHub API가 base64를 60자마다 줄바꿈(\n) 넣어서 반환하는데,
			//    Base64.getDecoder()(strict)는 줄바꿈이 섞이면 "Illegal base64 character 0xa"로 실패함.
			//    줄바꿈을 허용하는 MIME 디코더로 교체 (짧은 파일은 줄바꿈이 없어서 우연히 안 걸렸던 것)
			String decodedContent = new String(Base64.getMimeDecoder().decode(encodedContent), StandardCharsets.UTF_8);

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
		} catch (Exception e) {
			throw new RuntimeException("GitHub URL 저장 실패: " + e.getMessage());
		}
	}

	@Override
	public Map<String, Object> getGitHubBranches(String gitHubUrl) {
		try {
			// ✅ 60초 이내에 같은 저장소를 다시 조회하면 캐시된 결과를 반환 (rate limit 소진 방지)
			CachedBranchResult cached = branchCache.get(gitHubUrl);
			if (cached != null && !cached.isExpired()) {
				return cached.result;
			}

			String githubOwner = parseGitHubOwner(gitHubUrl);
			String githubRepo = parseGitHubRepo(gitHubUrl);

			String repoUrl = String.format("%s/repos/%s/%s", githubApiUrl, githubOwner, githubRepo);

			// ✅ 인증 헤더 적용 (익명 요청 60회/시간 → 토큰 사용 시 5,000회/시간)
			HttpEntity<String> entity = new HttpEntity<>(buildGitHubHeaders());
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

			// ✅ 캐시에 저장
			branchCache.put(gitHubUrl, new CachedBranchResult(result));

			return result;

		} catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
			// ✅ rate limit 등 403은 별도로 구분해서 원인을 명확히 알려줌
			throw new RuntimeException(
					"GitHub API 요청 한도를 초과했습니다(403). 잠시 후 다시 시도하거나 github.token 설정을 확인해주세요: " + e.getMessage());
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
			List<sourcerepositoryVO> commits = fetchCommitsFromGitHub(project.getGitUrl(), branch);

			// ✅ 커밋마다 개별 SELECT를 날리던 걸 "이미 있는 SHA 목록"을 한 번에 조회해서
			//    메모리(HashSet)에서 비교하는 방식으로 변경. 동기화가 느렸던 주된 원인이 이 부분이었음
			//    (커밋 30개 = SELECT 30번 → 이제는 SELECT 1번).
			List<String> existingHashes = mapper.selectExistingCommitHashesByProjectAndBranch(projectId, branch);
			java.util.Set<String> existingHashSet = new java.util.HashSet<>(existingHashes);

			// ✨ 신규 Commit 필터링
			List<sourcerepositoryVO> newCommits = new ArrayList<>();
			for (sourcerepositoryVO commit : commits) {
				boolean exists = existingHashSet.contains(commit.getCommitHash());
				if (!exists) {
					commit.setProjectId(projectId);
					commit.setBranchName(branch);
					commit.setCommitId(UUID.randomUUID().toString());
					commit.setCreatedAt(LocalDateTime.now());
					newCommits.add(commit);
				}
			}

			// ✨ 신규 Commit 저장
			int successCount = 0;
			int failCount = 0;
			List<String> failedShas = new ArrayList<>();

			if (!newCommits.isEmpty()) {
				// ✅ DB 컬럼(commit_message VARCHAR2(255))보다 긴 메시지가 들어오면
				//    ORA-12899로 INSERT 자체가 실패해서 해당 커밋이 통째로 누락되는 문제가 있었음.
				//    컬럼을 늘리는 게 근본 해결이지만, 늘리기 전까지 방어적으로 잘라서 저장.
				for (sourcerepositoryVO commit : newCommits) {
					String message = commit.getCommitMessage();
					if (message != null && message.length() > 255) {
						commit.setCommitMessage(message.substring(0, 252) + "...");
					}
				}

				// ✅ 빠른 경로: 한 번의 배치 INSERT로 전부 저장 시도 (DB 왕복 1번).
				//    커밋 개별 INSERT가 동기화가 느렸던 주된 원인이었음.
				try {
					mapper.insertSourcerepositoryCommits(newCommits);
					successCount = newCommits.size();
				} catch (Exception batchEx) {
					// ✅ 느린 경로(폴백): 배치 전체가 실패한 경우에만 한 건씩 다시 시도해서
					//    문제가 되는 커밋만 정확히 걸러내고 나머지는 살림
					System.err.println("⚠️ 배치 INSERT 실패, 개별 저장으로 재시도: " + batchEx.getMessage());
					for (sourcerepositoryVO commit : newCommits) {
						try {
							mapper.insertSourcerepositoryCommit(commit);
							successCount++;
						} catch (Exception e) {
							failCount++;
							failedShas.add(commit.getCommitHash());
							System.err.println("❌ Commit 저장 실패: " + commit.getCommitHash() + " - " + e.getMessage());
						}
					}
				}
			}

			// ✅ 일부라도 저장 실패한 게 있으면 콘솔에만 남기지 않고 예외로 알림
			//    (컨트롤러가 이 메시지를 flash message로 화면에 띄워줌)
			if (failCount > 0) {
				throw new RuntimeException(String.format(
						"%d건 저장 성공, %d건 저장 실패 (SHA: %s%s). 서버 콘솔 로그에서 상세 원인을 확인하세요.",
						successCount, failCount,
						String.join(", ", failedShas.subList(0, Math.min(3, failedShas.size()))),
						failedShas.size() > 3 ? " 외 " + (failedShas.size() - 3) + "건" : ""));
			}

		} catch (Exception e) {
			throw new RuntimeException("GitHub 동기화 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * ✅ 4-1. 저장소의 모든 브랜치를 한 번에 동기화.
	 *    내부적으로 syncSourcerepositoryFromGitHub()를 브랜치 수만큼 반복 호출.
	 *    한 브랜치가 실패해도 나머지 브랜치는 계속 진행하도록 브랜치별로 개별 try-catch 처리.
	 */
	@Override
	public Map<String, Object> syncAllBranchesFromGitHub(String projectId) {
		ProjectVO project = projectService.selectProjectDetail(projectId);
		if (project == null || project.getGitUrl() == null || project.getGitUrl().isEmpty()) {
			throw new RuntimeException("GitHub URL이 설정되지 않았습니다.");
		}

		// 1. 저장소의 실제 브랜치 목록 조회 (60초 캐시 있는 getGitHubBranches 재사용)
		Map<String, Object> branchInfo = getGitHubBranches(project.getGitUrl());
		@SuppressWarnings("unchecked")
		List<Map<String, String>> branches = (List<Map<String, String>>) branchInfo.get("branches");

		// ✅ DB 왕복은 이미 줄였지만, 브랜치마다 필요한 GitHub API 호출(네트워크 왕복)이
		//    순차 처리라 진짜 병목이었음(브랜치 7개 × ~1.5초 = 10초 이상).
		//    브랜치끼리는 서로 독립적이라 동시에 처리해도 안전 → 스레드풀로 병렬 처리.
		List<String> succeeded = java.util.Collections.synchronizedList(new ArrayList<>());
		List<String> failed = java.util.Collections.synchronizedList(new ArrayList<>());

		// ⚠️ 20개로 올렸더니 GitHub API의 "동시 요청 남용 탐지(secondary rate limit)"에 걸려서
		//    순차 처리(10초)보다도 훨씬 느린 30초+로 나왔음. GitHub은 같은 저장소에 대한
		//    과도한 동시 요청 자체를 이상 트래픽으로 보고 고의로 응답을 지연시킴.
		//    (공식 문서: "avoid making concurrent requests" 권고)
		//    5도 애매해서 더 보수적으로 3까지 낮춤 - 필요하면 여기 숫자만 조절해서 재실험 가능.
		int poolSize = Math.min(branches.size(), 5);
		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(poolSize);
		try {
			List<java.util.concurrent.CompletableFuture<Void>> futures = new ArrayList<>();
			for (Map<String, String> b : branches) {
				String branchName = b.get("name");
				futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> {
					try {
						syncSourcerepositoryFromGitHub(projectId, branchName);
						succeeded.add(branchName);
					} catch (Exception e) {
						failed.add(branchName + "(" + e.getMessage() + ")");
						System.err.println("⚠️ 브랜치 동기화 실패: " + branchName + " - " + e.getMessage());
					}
				}, executor));
			}
			// 모든 브랜치 작업이 끝날 때까지 대기 (한 브랜치 실패해도 나머지는 계속 진행됨)
			java.util.concurrent.CompletableFuture
					.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
		} finally {
			executor.shutdown();
		}

		Map<String, Object> result = new HashMap<>();
		result.put("totalBranches", branches.size());
		result.put("succeeded", succeeded);
		result.put("failed", failed);
		return result;
	}

	private List<sourcerepositoryVO> fetchCommitsFromGitHub(String gitUrl, String branch) {
		return fetchCommitsFromGitHub(gitUrl, branch, null);
	}

	// ✅ path 파라미터 추가 버전. path가 있으면 GitHub Commits API의 "path" 쿼리로 필터링해서
	//    "이 파일 경로를 건드린 커밋들만" 조회함 (GitHub REST API 공식 문서: /rest/commits/commits)
	private List<sourcerepositoryVO> fetchCommitsFromGitHub(String gitUrl, String branch, String path) {
		try {
			String owner = parseGitHubOwner(gitUrl);
			String repo = parseGitHubRepo(gitUrl);

			if (owner == null || repo == null) {
				throw new RuntimeException("GitHub URL 파싱 실패");
			}

			// ✅ URLEncoder는 "/"까지 %2F로 인코딩해서 GitHub이 path 값을 못 알아듣게 만들 수 있었음.
			//    UriComponentsBuilder로 표준적인 쿼리 파라미터 인코딩을 사용하도록 변경.
			org.springframework.web.util.UriComponentsBuilder uriBuilder = org.springframework.web.util.UriComponentsBuilder
					.fromHttpUrl(githubApiUrl + "/repos/" + owner + "/" + repo + "/commits")
					.queryParam("sha", branch)
					.queryParam("per_page", 30);
			if (path != null && !path.isEmpty()) {
				uriBuilder.queryParam("path", path);
			}
			String apiUrl = uriBuilder.build().encode().toUriString();

			// ✅ 인증 헤더 적용
			HttpEntity<String> entity = new HttpEntity<>(buildGitHubHeaders());
			ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
			String response = responseEntity.getBody();
			JsonArray commits = JsonParser.parseString(response).getAsJsonArray();
			List<sourcerepositoryVO> commitList = new ArrayList<>();

			for (int i = 0; i < commits.size(); i++) {
				JsonObject commit = commits.get(i).getAsJsonObject();

				try {
					sourcerepositoryVO vo = new sourcerepositoryVO();
					vo.setCommitHash(commit.get("sha").getAsString());

					JsonObject commitDetail = commit.getAsJsonObject("commit");
					vo.setCommitMessage(commitDetail.has("message") ? commitDetail.get("message").getAsString() : "");

					// ✅ author 객체 또는 name/email이 없는 커밋(봇 커밋, GPG 서명 커밋 등)이 있을 수 있음.
					//    author_name은 DB NOT NULL이라 null이면 그 자체로 INSERT가 실패하므로 기본값 보정.
					//    또한 하나의 커밋 파싱 실패가 전체 30건 조회를 통째로 실패시키지 않도록 개별 try-catch로 격리.
					JsonObject authorObj = commitDetail.has("author") && !commitDetail.get("author").isJsonNull()
							? commitDetail.getAsJsonObject("author")
							: null;

					String authorName = (authorObj != null && authorObj.has("name") && !authorObj.get("name").isJsonNull())
							? authorObj.get("name").getAsString()
							: "Unknown";
					String authorEmail = (authorObj != null && authorObj.has("email") && !authorObj.get("email").isJsonNull())
							? authorObj.get("email").getAsString()
							: null;
					vo.setAuthorName(authorName);
					vo.setAuthorEmail(authorEmail);

					String committedAt = (authorObj != null && authorObj.has("date") && !authorObj.get("date").isJsonNull())
							? authorObj.get("date").getAsString()
							: null;
					if (committedAt != null) {
						vo.setCommittedAt(LocalDateTime.parse(committedAt.replace("Z", "+00:00"),
								java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME).withNano(0));
					} else {
						// committed_at도 DB NOT NULL이라 값이 없으면 현재 시각으로 대체
						vo.setCommittedAt(LocalDateTime.now().withNano(0));
					}

					commitList.add(vo);
				} catch (Exception rowEx) {
					// 커밋 1건 파싱 실패는 그 커밋만 건너뛰고 나머지는 계속 진행
					System.err.println("⚠️ GitHub 커밋 파싱 실패(건너뜀): " + rowEx.getMessage());
				}
			}

			return commitList;

		} catch (Exception e) {
			throw new RuntimeException("GitHub Commit 조회 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * ✅ 파일 경로 기준 Commit 이력 조회 (DB 저장 없이 GitHub에서 실시간 조회)
	 *    commit_log 테이블은 브랜치 단위로만 커밋을 저장하고 "어떤 파일이 바뀌었는지"는 안 갖고 있어서,
	 *    파일별 이력은 DB로 못 찾고 GitHub Commits API의 path 파라미터로 매번 직접 조회해야 함.
	 */
	@Override
	public List<sourcerepositoryVO> getFileCommitHistory(String projectId, String filePath, String branch) {
		try {
			ProjectVO project = projectService.selectProjectDetail(projectId);
			if (project == null || project.getGitUrl() == null || project.getGitUrl().isEmpty()) {
				throw new RuntimeException("GitHub URL이 설정되지 않았습니다.");
			}
			return fetchCommitsFromGitHub(project.getGitUrl(), branch, filePath);
		} catch (Exception e) {
			throw new RuntimeException("파일별 Commit 이력 조회 실패: " + e.getMessage(), e);
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
			return mapper.selectSourcerepositoryCommitBySha(commitHash, null);

		} catch (Exception e) {
			throw new RuntimeException("Commit 상세 조회 실패: " + e.getMessage());
		}
	}

	/**
	 * 7. 단일 Commit 조회
	 * ✅ branchName으로 좁혀서 조회 (같은 SHA가 여러 브랜치에 있을 수 있음). null이면 최신 1건.
	 */
	@Override
	public sourcerepositoryVO getSourcerepositoryCommit(String commitHash, String branchName) {
		return mapper.selectSourcerepositoryCommitBySha(commitHash, branchName);
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

	@Override
	public String getFirstBranchByProjectId(String projectId) {
		try {
			ProjectVO project = projectService.selectProjectDetail(projectId);
			String gitUrl = project != null ? project.getGitUrl() : null;

			// ✅ "DB에 동기화된 아무 브랜치나 하나"가 아니라, GitHub이 실제로 알려주는 기본 브랜치를 우선 사용.
			//    기존엔 selectFirstBranchNameByProjectId가 ORDER BY 없이 조회해서
			//    (Oracle은 정렬 기준이 없으면 순서를 보장하지 않음) 한 번 동기화된 브랜치(예: dev)가
			//    실제 기본 브랜치(main)와 무관하게 계속 고정되어 나오는 문제가 있었음.
			if (gitUrl != null && !gitUrl.isEmpty()) {
				try {
					Map<String, Object> branchInfo = getGitHubBranches(gitUrl);
					String defaultBranch = (String) branchInfo.get("defaultBranch");
					if (defaultBranch != null && !defaultBranch.isEmpty()) {
						return defaultBranch;
					}
				} catch (Exception githubEx) {
					System.err.println("⚠️ GitHub 기본 브랜치 조회 실패, DB 값으로 폴백: " + githubEx.getMessage());
				}
			}

			// ✅ 폴백: GitHub URL이 없거나 GitHub 호출이 실패한 경우에만 DB에 동기화된 브랜치라도 사용
			String branch = mapper.selectFirstBranchNameByProjectId(projectId);
			if (branch == null || branch.isEmpty()) {
				throw new RuntimeException("동기화된 브랜치가 없고 GitHub 조회도 실패했습니다");
			}
			return branch;

		} catch (Exception e) {
			throw new RuntimeException("Branch 조회 실패: " + e.getMessage(), e);
		}
	}

}