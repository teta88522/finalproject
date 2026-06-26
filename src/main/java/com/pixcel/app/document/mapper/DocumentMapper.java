package com.pixcel.app.document.mapper;

import java.util.List;

import com.pixcel.app.document.service.DocumentCategoryVO;
import com.pixcel.app.document.service.DocumentHistoryVO;
import com.pixcel.app.document.service.DocumentVO;

public interface DocumentMapper {
	
	public List<DocumentVO> selectAll();
	public DocumentVO selectHistoryDetail(String documentHistoryId);
	public int insertDocument(DocumentVO documentVO);
	public int insertDocumentHistory(DocumentHistoryVO documentHistoryVO);
	public int updateDocument(DocumentVO documentVO);
	public int selectNextDocumentVersion(String documentId);
	public List<DocumentHistoryVO> selectHistoryAll(String documentId);
	public List<DocumentCategoryVO> selectCategoryAll(String projectId);
	public List<DocumentVO> selectNoCategory(String projectId);
	public List<DocumentVO> selectCategorydoc(String categoryId);
	public DocumentVO selectDetail(String documentId);
	public int insertCategory(DocumentCategoryVO documentCategoryVO);
}
