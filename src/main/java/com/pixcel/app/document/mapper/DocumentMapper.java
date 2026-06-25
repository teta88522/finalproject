package com.pixcel.app.document.mapper;

import java.util.List;

import com.pixcel.app.document.service.DocumentCategoryVO;
import com.pixcel.app.document.service.DocumentHistoryVO;
import com.pixcel.app.document.service.DocumentVO;

public interface DocumentMapper {
	
	public List<DocumentVO> selectAll();
	public int insertDocument(DocumentVO documentVO);
	public List<DocumentHistoryVO> selectHistoryAll();
	public List<DocumentCategoryVO> selectCategoryAll();
	public List<DocumentVO> selectNoCategory();
	public List<DocumentVO> selectCategorydoc(String categoryId);
	public DocumentVO selectDetail(String documentId);
}
