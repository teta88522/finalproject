package com.pixcel.app.file.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.file.mapper.FileMapper;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileDownloadHistoryVO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class FileServiceImpl implements FileService{
	
	@Autowired
	FileMapper fileMapper;

	@Value("${file.dir}")
	private String fileDir;	

	@Override
	@Transactional
	public int uploadFile(List<MultipartFile> files, FileDTO req) {
		if (files == null || files.isEmpty() || req == null) {
            return 0;
        }
		
		File uploadDir = new File(fileDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        
		int count = 0;
		
		for(MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
                continue;
            }
			try {
					String originName = file.getOriginalFilename();
					if (!StringUtils.hasText(originName)) {
	                    continue;
	                }
					String projectId =  req.getProjectId();
					String connectAddress = req.getConnectAddress();
					int nextVersion = fileMapper.selectNextFileVersion(req.getProjectId(),
			        req.getConnectAddress(),
					        originName);
					
					String uuid = UUID.randomUUID().toString();
					String saveName  = uuid + "_" + originName;
					File dest = new File(fileDir, saveName);
					file.transferTo(dest);
					String saveFileDir = fileDir + saveName;
					
					FileVO vo = new FileVO();
					vo.setProjectId(projectId);
					vo.setVersionId(req.getVersionId());
					vo.setFileCode(req.getFileCode());
					vo.setOriginalName(originName);
					vo.setStoredName(saveName);
					vo.setFilePath(saveFileDir);
					vo.setFileSize(String.valueOf(file.getSize()));
					vo.setUploadUserId(req.getUploadUserId());
					vo.setFileVersion(nextVersion);
					vo.setConnectAddress(connectAddress);
					System.out.print(vo);
					fileMapper.insertFile(vo);
					
					count++;
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				
		}
		return count;
	}


	@Override
	@Transactional
	public void downloadAll(String connectAddress, HttpServletResponse response, String userId) throws IOException {
		List<FileVO> files = fileMapper.downloadAll(connectAddress);
		
		if(files == null || files.isEmpty()) {
			throw new RuntimeException("다운로드할 파일이 없습니다.");
		}
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition",
	            "attachment; filename=\"files.zip\"");
		
		try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
			byte[] buffer = new byte[8192];
			
			for(FileVO fileInfo : files) {
				
				
				File file = new File(fileInfo.getFilePath());
				
				if(!file.exists()) {
					continue;
				}
				FileDownloadHistoryVO historyFile = new FileDownloadHistoryVO();
				historyFile.setFileId(fileInfo.getFileId());
				historyFile.setDownloadUserId(userId);
				fileMapper.insertDownloadHistory(historyFile);
				
				ZipEntry zipEntry = new ZipEntry(fileInfo.getOriginalName());
				
				zos.putNextEntry(zipEntry);
				
				try (FileInputStream fis = new FileInputStream(file)){
					int length;
					
					while((length = fis.read(buffer)) != -1) {
						zos.write(buffer, 0, length);
					}
				}
				zos.closeEntry();
			}
			zos.finish();
		}
	}

	@Override
	public void downloadOne(String fileId, HttpServletResponse response, String userId) throws IOException {
		FileVO fileInfo = fileMapper.downloadOne(fileId);

		if(fileInfo == null) {
			throw new RuntimeException("다운로드할 파일이 없습니다.");
		}
		File file = new File(fileInfo.getFilePath());
		if(!file.exists()) {
			throw new RuntimeException("실제 파일이 존재하지 않습니다.");
		}
		FileDownloadHistoryVO historyFile = new FileDownloadHistoryVO();
		historyFile.setFileId(fileInfo.getFileId());
		historyFile.setDownloadUserId(userId);
		fileMapper.insertDownloadHistory(historyFile);
		
		String fileName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8).replace("\\+", "%20");
		response.setContentType("application/octet-stream");
		response.setContentLengthLong(file.length());
		
		response.setHeader("Content-Disposition",
	            "attachment; filename=\"" + fileName + "\"");
		
		try(InputStream is = new FileInputStream(file); OutputStream os = response.getOutputStream()){
			byte[] buffer = new byte[8192];
			int length;
			
			while((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			os.flush();
		}
		
	}


	@Override
	public List<FileVO> selectAll(String connectAddress) {
		return fileMapper.downloadAll(connectAddress);
	}
	
}
