package com.pixcel.app.document.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixcel.app.document.mapper.DocumentMapper;
import com.pixcel.app.document.service.DocumentHistoryVO;
import com.pixcel.app.document.service.DocumentService;
import com.pixcel.app.document.service.DocumentVO;

@Service
public class DocumentServiceImpl implements DocumentService{
	
	@Autowired
	DocumentMapper documentMapper;
	
	@Override
	public List<DocumentVO> findAll() {
		return documentMapper.selectAll();
	}

	@Override
	public int addDocument(DocumentVO documentVO) {
		return documentMapper.insertDocument(documentVO);
	}

	@Override
	public List<DocumentHistoryVO> findHistoryAll() {
		return documentMapper.selectHistoryAll();
	}

}
