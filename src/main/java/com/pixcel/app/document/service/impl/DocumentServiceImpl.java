package com.pixcel.app.document.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixcel.app.document.mapper.DocumentMapper;
import com.pixcel.app.document.service.DocumentCategoryVO;
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
	public List<DocumentCategoryVO> selectCategoryAll() {
		return documentMapper.selectCategoryAll();
	}

	@Override
	public List<DocumentVO> selectNoCategory() {
		return documentMapper.selectNoCategory();
	}

	@Override
	public List<DocumentVO> selectCategorydoc(String categoryId) {
		return documentMapper.selectCategorydoc(categoryId);
	}

	@Override
	public DocumentVO selectDetail(String documentId) {
		return documentMapper.selectDetail(documentId);
	}

	@Override
	public int insertCategory(DocumentCategoryVO documentCategoryVO) {
		return documentMapper.insertCategory(documentCategoryVO);
	}

	@Override
	public int addDocumentHistory(DocumentHistoryVO documentHistoryVO) {
		return documentMapper.insertDocumentHistory(documentHistoryVO);
	}

	@Override
	public int selectNextDocumentVersion(String documentId) {
		return documentMapper.selectNextDocumentVersion(documentId);
	}

	@Override
	public int updateDocument(DocumentVO documentVO) {
		return documentMapper.updateDocument(documentVO);
	}

	@Override
	public List<DocumentHistoryVO> selectHistoryAll(String documentId) {
		// TODO Auto-generated method stub
		return documentMapper.selectHistoryAll(documentId);
	}

	@Override
	public DocumentVO selectHistoryDetail(String documentHistoryId) {
		// TODO Auto-generated method stub
		return documentMapper.selectHistoryDetail(documentHistoryId);
	}




}
