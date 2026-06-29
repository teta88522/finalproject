package com.pixcel.app.repository.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

//import com.pixcel.app.project.service.ProjectMemberVO;
//import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.repository.mapper.RepositoryMapper;
import com.pixcel.app.repository.service.RepositoryService;
import com.pixcel.app.repository.service.RepositoryVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // lombok을 이용해 RepositoryMapper의 생성자 주입을 자동으로 처리
public class RepositoryServiceImpl implements RepositoryService {
	private final RepositoryMapper repositoryMapper;
//	private final ProjectService projectService;

	// 파일이 저장될 서버 경로 (임시지정)
	@Value("${file.dir}")
	private String savePath;

	// 1. 자료실 전체 조회
	@Override
	public List<RepositoryVO> getRepositoryList(RepositoryVO searchVO) {
		return repositoryMapper.selectRepositoryList(searchVO);
	}

	// 2. 자료실 등록한 사람의 자료 상세조회
	@Override
	public List<RepositoryVO> getRepositoryListByUserId(String uploadUserId) {
		return repositoryMapper.selectRepositoryListByUserId(uploadUserId);
	}

	// 3. 자료실 상세조회
	@Override
	public RepositoryVO getRepositoryDetail(String fileId) {
		return repositoryMapper.selectRepositoryDetail(fileId);
	}

	// 4. 자료실 파일 등록
	@Override
	public int registerRepository(RepositoryVO repositoryVO, MultipartFile uploadFile) {

		// 1. 프로젝트 ID 검증[cite: 14]
		if (repositoryVO.getProjectId() == null || repositoryVO.getProjectId().isEmpty()) {
			throw new RuntimeException("프로젝트 ID가 전달되지 않았습니다. 폼 데이터를 확인하세요.");
		}

		// 2. [추가] VERSION_ID가 없을 경우 DB에서 조회하여 자동 세팅[cite: 14]
		if (repositoryVO.getVersionId() == null || repositoryVO.getVersionId().isEmpty()) {
			String versionId = repositoryMapper.selectRecentVersionIdByProjectId(repositoryVO.getProjectId());
			
			// --- 방어 코드 추가 ---
	        if (versionId == null) {
	            throw new RuntimeException("해당 프로젝트(" + repositoryVO.getProjectId() + ")에 연결된 버전 정보가 없습니다. 프로젝트 버전을 먼저 생성해주세요.");
	        }
			
			repositoryVO.setVersionId(versionId);
		}

		// 3. 파일 처리[cite: 14]
		if (uploadFile != null && !uploadFile.isEmpty()) {
			handleFileUpload(repositoryVO, uploadFile);
		}

		// 4. 저장[cite: 14]
		return repositoryMapper.insertRepository(repositoryVO);
	}

	// 5. 자료실 파일 수정
	@Override
	@Transactional
	public int modifyRepository(RepositoryVO repositoryVO, MultipartFile uploadFile, String userId) {
		// 1. 기존 데이터 확인 (팀원 코드의 target 찾기)
		RepositoryVO oldData = repositoryMapper.selectRepositoryDetail(repositoryVO.getFileId());

		// 2. 기존 데이터 확인
		if (oldData == null) {
			throw new RuntimeException("존재하지 않는 파일입니다.");
		}

		// 3. 권한 체크 (파일을 등록한 사용자만 수정 가능)
		if (!oldData.getUploadUserId().equals(userId)) {
			throw new RuntimeException("파일을 등록한 사용자만 수정할 수 있습니다.");
		}

		// 4. 기존 파일 비활성화
		repositoryMapper.updateRepositoryUseYn(repositoryVO.getFileId(), "N");

		// 5. 새 버전 정보 세팅
		int nextVersion = Integer.parseInt(oldData.getFileVersion()) + 1;
		repositoryVO.setFileVersion(String.valueOf(nextVersion));
		repositoryVO.setProjectId(oldData.getProjectId());
		repositoryVO.setFileCode(oldData.getFileCode());
		repositoryVO.setVersionId(oldData.getVersionId());

		// 6. 새 파일 저장
		return registerRepository(repositoryVO, uploadFile);
	}

	// 파일 업로드 공통 처리 로직
	private void handleFileUpload(RepositoryVO repositoryVO, MultipartFile uploadFile) {
		String originalName = uploadFile.getOriginalFilename();
		String ext = originalName.substring(originalName.lastIndexOf("."));

		// 중복방지를 위한 물리 파일명 생성 (UUID)
		String storedName = UUID.randomUUID().toString() + ext;

		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		try {
			uploadFile.transferTo(new File(savePath + storedName));

			// VO 객체에 파일 메타데이터 세팅
			repositoryVO.setOriginalName(originalName);
			repositoryVO.setStoredName(storedName);
			repositoryVO.setFilePath(savePath + storedName);
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public int removeRepository(String fileId, String userId) {
		RepositoryVO target = repositoryMapper.selectRepositoryDetail(fileId);
		if (target == null || !target.getUploadUserId().equals(userId)) {
			throw new RuntimeException("본인이 작성한 자료만 삭제가 가능합니다.");
		}

		return repositoryMapper.deleteRepository(fileId);
	}

	@Override
	public int getTotalCount(RepositoryVO searchVO) {
		return repositoryMapper.selectRepositoryTotalCount(searchVO);
	}

	@Override
	public boolean isProjectMember(String projectId, String userId) {
		int count = repositoryMapper.isProjectMember(projectId, userId);

		return count > 0;
	}
	
	@Override
	public List<Map<String, Object>> getSourceCodeList(){
		return repositoryMapper.selectSourceCodeList();
	}

}