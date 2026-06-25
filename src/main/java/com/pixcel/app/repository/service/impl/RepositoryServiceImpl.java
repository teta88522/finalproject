package com.pixcel.app.repository.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.project.service.ProjectMemberVO;
import com.pixcel.app.project.service.ProjectService;
import com.pixcel.app.repository.mapper.RepositoryMapper;
import com.pixcel.app.repository.service.RepositoryService;
import com.pixcel.app.repository.service.RepositoryVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // lombok을 이용해 RepositoryMapper의 생성자 주입을 자동으로 처리
public class RepositoryServiceImpl implements RepositoryService {
	private final RepositoryMapper repositoryMapper;
	private final ProjectService projectService;

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

		// 파일 드롭존이나 첨부파일을 통해 파일이 넘어왔을 대만 처리함
		if (uploadFile != null && !uploadFile.isEmpty()) {
			handleFileUpload(repositoryVO, uploadFile);
		}

		// 파일 업로드 로직이나 다중 비즈니스 처리가 필요하면 이곳에서 작성
		return repositoryMapper.insertRepository(repositoryVO);
	}

	// 5. 자료실 파일 수정
	@Override
	@Transactional
	public int modifyRepository(RepositoryVO repositoryVO, MultipartFile uploadFile, String userId) {
	    // 1. 기존 데이터 확인 (팀원 코드의 target 찾기)
	    RepositoryVO oldData = repositoryMapper.selectRepositoryDetail(repositoryVO.getFileId());
	    
	    // 2. 권한 체크 (이건 필수!)
	    List<ProjectMemberVO> members = projectService.selectProjectMemberList(oldData.getProjectId());
	    boolean isMember = members.stream().anyMatch(member -> member.getUserId().equals(userId));
	    if (!isMember) {
	        throw new RuntimeException("프로젝트 구성원만 수정할 수 있습니다.");
	    }

	    // 3. 기존 파일 죽이기
	    repositoryMapper.updateRepositoryUseYn(repositoryVO.getFileId(), "N");

	    // 4. 새 버전 정보 세팅
	    int nextVersion = Integer.parseInt(oldData.getFileVersion()) + 1;
	    repositoryVO.setFileVersion(String.valueOf(nextVersion));
	    repositoryVO.setProjectId(oldData.getProjectId());
	    repositoryVO.setFileCode(oldData.getFileCode());
	    repositoryVO.setVersionId(oldData.getVersionId());

	    // 5. 새 파일 저장
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

}
