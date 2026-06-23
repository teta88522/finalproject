package com.pixcel.app.file.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.file.mapper.FileMapper;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;

@Service
public class FileServiceImpl implements FileService{
	
	@Autowired
	FileMapper fileMapper;

	@Value("${file.dir}")
	private String fileDir;	
	
	@Override
	public List<FileVO> findAll(String connectAddress) {
		return fileMapper.selectAll(connectAddress);
	}

	@Override
	public int uploadFile(List<MultipartFile> files, FileDTO req) {
		
		if(files == null || files.isEmpty()) {
			return 0;
		}
		
		int count = 0;
		
		for(MultipartFile file : files) {
			try {
					String originName = file.getOriginalFilename();
					
					String projectId =  req.getProjectId();
					String connectAddress = req.getConnectAddress();
					int maxVersion = fileMapper.selectMaxVersion(originName, projectId, connectAddress);
					
					int nextVersion = maxVersion + 1;
					
					String uuid = UUID.randomUUID().toString();
					String saveName  = uuid + "_" + originName;
					
					File dest = new File(fileDir, saveName);
					file.transferTo(dest);
					
					FileVO vo = new FileVO();
					vo.setProjectId(projectId);
					vo.setVersionId(req.getVersionId());
//					vo.setFileCode(req.getFileCode());
					vo.setOriginalName(originName);
					vo.setStoredName(saveName);
					vo.setFilePath(fileDir);
					vo.setFileSize(String.valueOf(file.getSize()));
					vo.setUploadUserId(req.getUploadUserId());
					vo.setFileVersion(nextVersion);
					vo.setConnectAddress(connectAddress);
					
					fileMapper.insertFile(vo);
					
					count++;
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				
		}
		return count;
	}
	
}
