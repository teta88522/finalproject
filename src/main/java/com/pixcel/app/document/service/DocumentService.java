package com.pixcel.app.document.service;

import java.util.List;

public interface DocumentService {
	
	public List<DocumentVO> findAll();
	public List<DocumentHistoryVO> selectHistoryAll(String documentId);
	public int addDocument(DocumentVO documentVO);
	public int addDocumentHistory(DocumentHistoryVO documentHistoryVO);
	public int updateDocument(DocumentVO documentVO);
	public int selectNextDocumentVersion(String documentId);
	public List<DocumentCategoryVO> selectCategoryAll();
	public List<DocumentVO> selectNoCategory();
	public List<DocumentVO> selectCategorydoc(String categoryId);
	public DocumentVO selectDetail(String documentId);
	public DocumentVO selectHistoryDetail(String documentHistoryId);
	public int insertCategory(DocumentCategoryVO documentCategoryVO);
}
