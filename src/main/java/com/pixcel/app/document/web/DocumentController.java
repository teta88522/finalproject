package com.pixcel.app.document.web;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.codevalue.service.CodeValueService;
import com.pixcel.app.codevalue.service.CodeValueVO;
import com.pixcel.app.document.service.DocumentCategoryVO;
import com.pixcel.app.document.service.DocumentHistoryVO;
import com.pixcel.app.document.service.DocumentService;
import com.pixcel.app.document.service.DocumentVO;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.milestones.service.MilestoneSearchVO;
import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;
import com.pixcel.app.web.AllProjectController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@AllProjectController
@Controller
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {
	
	Logger logger = LoggerFactory.getLogger(DocumentController.class);
	private final DocumentService documentService;
	private final FileService fileService;
	private final MilestonesService milestonesService;
	private final CodeValueService codeValueService;
	
	@GetMapping("/list")
    public String documentList(Model model,@PathVariable String projectId, @CookieValue(value="userId", required =false)String userId) {
	    List<DocumentCategoryVO> categoryList = documentService.selectCategoryAll(projectId);
	    model.addAttribute("categoryList",categoryList);
	    int total = 0;
	    if (categoryList != null && !categoryList.isEmpty()) {
	    	total = categoryList.get(0).getTotalCnt();
	    }
	    model.addAttribute("total",total);
	    System.out.println(projectId);
	    List<DocumentVO> categoryNoList = documentService.selectNoCategory(projectId);
	    model.addAttribute("categoryNoList",categoryNoList);
	    int noTotal = 0;

	    if (categoryNoList != null && !categoryNoList.isEmpty()) {
	    	noTotal = categoryNoList.get(0).getTotalCnt();
	    }
	    model.addAttribute("noTotal",noTotal);
	    model.addAttribute("projectId",projectId);
        return "document/documentList";
    }
	
	@GetMapping("/list/{categoryId}")
    public String documentCategoryList(Model model, @CookieValue(value="userId", required =false)String userId,@PathVariable("projectId") String projectId, @PathVariable("categoryId") String categoryId) {
		System.out.println(categoryId);
	    List<DocumentVO> categorydocList = documentService.selectCategorydoc(categoryId);
	    System.out.println(categorydocList.size());
	    model.addAttribute("categorydocList",categorydocList);
        return "document/documentCategoryList";
    }
	
	@GetMapping("/add")
    public String documentAdd(Model model,@PathVariable("projectId") String projectId, @CookieValue(value="userId", required =false)String userId) {
		MilestoneSearchVO vo = new MilestoneSearchVO();
		vo.setProjectId("PROJECT_ID_2606_0001");
		List<MilestonesVO> milestoneList = milestonesService.getMilestoneList(vo.getProjectId());

	    model.addAttribute("milestoneList", milestoneList);
	    
	    List<CodeValueVO> codeValueList = codeValueService.getCodeValueListByGroup(userId,"g003");
	    model.addAttribute("codeValueList", codeValueList);
	    
	    List<DocumentCategoryVO> categoryList = documentService.selectCategoryAll(projectId);
	    model.addAttribute("categoryList",categoryList);
	    
	    
	    
        return "document/documentAdd";
    }
	
	@PostMapping("/add")
	@Transactional
    public String documentAddProc(@CookieValue(value="userId", required =false)String userId, DocumentVO documentVO, @PathVariable("projectId") String projectId, @RequestParam("files") List<MultipartFile> files) {
		
		int versionId = documentService.selectNextDocumentVersion(documentVO.getDocumentId());
		logger.debug(documentVO.toString());
		System.out.print(documentVO);
		documentVO.setCreatedBy(userId);
		documentService.addDocument(documentVO);
		System.out.println(documentVO.getDocumentId());
		
		
		
		DocumentHistoryVO documentHistoryVO = new DocumentHistoryVO();
		documentHistoryVO.setDocumentId(documentVO.getDocumentId());
		documentHistoryVO.setCreatedBy(documentVO.getCreatedBy());
		documentHistoryVO.setTitle(documentVO.getTitle());
		documentHistoryVO.setDescription(documentVO.getDescription());
		documentHistoryVO.setDocumentVersionId(versionId);
		documentService.addDocumentHistory(documentHistoryVO);
		
		
		FileDTO uploadDTO = new FileDTO();
		uploadDTO.setProjectId(projectId);
		uploadDTO.setVersionId(documentVO.getVersionId());
		uploadDTO.setFileCode("f001");
		uploadDTO.setUploadUserId(userId);
		uploadDTO.setConnectAddress(documentVO.getDocumentId());

		fileService.uploadFile(files, uploadDTO);
		
		System.out.print(documentVO.getDocumentId() + "문서 등록");
        return "redirect:/project/" + projectId +"/document/list";
    }
	
	@GetMapping("/detail/{documentId}")
    public String documentDetail(Model model,@PathVariable("projectId") String projectId, @CookieValue(value="userId", required =false)String userId, @PathVariable String documentId) {
		
		System.out.println(documentId);
	    DocumentVO docDetail = documentService.selectDetail(documentId);
	    model.addAttribute("docDetail",docDetail);
	    List<FileVO> fileList = fileService.selectAll(documentId);
	    model.addAttribute("fileList",fileList);
        return "document/documentDetail";
    }
	
	@GetMapping("/detail/{documentId}/download")
	public void downloadFileAll(@PathVariable String documentId,HttpServletResponse response, @PathVariable("projectId") String projectId,@CookieValue(value="userId", required =false)String userId) throws IOException{
		fileService.downloadAll(documentId, response, userId);
	}
	
	@GetMapping("/detail/{documentId}/{fileId}/download")
	public void downloadFile(@PathVariable String fileId,HttpServletResponse response, @PathVariable("projectId") String projectId,@CookieValue(value="userId", required =false)String userId) throws IOException{
		fileService.downloadOne(fileId, response, userId);
	}
	
	
	@GetMapping("/update/{documentId}")
    public String documentUpdate(Model model, @PathVariable("projectId") String projectId, @CookieValue(value="userId", required =false)String userId, @PathVariable String documentId) {
		System.out.println(documentId);
	    DocumentVO docDetail = documentService.selectDetail(documentId);
	    model.addAttribute("docDetail",docDetail);
	    int documentVersionId = documentService.selectNextDocumentVersion(documentId);
	    model.addAttribute("documentVersionId",documentVersionId);
	    List<FileVO> fileList = fileService.selectAll(documentId);
	    model.addAttribute("fileList",fileList);
        return "document/documentUpdate";
    }
	
	@PostMapping("/update/{documentId}")
	@Transactional
    public String documentUpdateProc(@CookieValue(value="userId", required =false)String userId, DocumentVO documentVO, @PathVariable("projectId") String projectId, @RequestParam("files") List<MultipartFile> files, @PathVariable String documentId) {
		
		int versionId = documentService.selectNextDocumentVersion(documentId);
		DocumentVO document = documentService.selectDetail(documentId);
		logger.debug(documentVO.toString());
		System.out.print(documentVO);
		documentVO.setCreatedBy(userId);
		documentVO.setDocumentVersionId(versionId);
		documentService.updateDocument(documentVO);
		System.out.println(documentVO.getDocumentId());
		
		
		
		DocumentHistoryVO documentHistoryVO = new DocumentHistoryVO();
		documentHistoryVO.setDocumentId(documentVO.getDocumentId());
		documentHistoryVO.setCreatedBy(documentVO.getCreatedBy());
		documentHistoryVO.setTitle(documentVO.getTitle());
		documentHistoryVO.setDescription(documentVO.getDescription());
		documentHistoryVO.setDocumentVersionId(versionId);
		documentService.addDocumentHistory(documentHistoryVO);
		
		
		FileDTO uploadDTO = new FileDTO();
		uploadDTO.setProjectId("PROJECT_ID_2606_0001");
		uploadDTO.setVersionId(document.getVersionId());
		uploadDTO.setFileCode("f001");
		uploadDTO.setUploadUserId(userId);
		uploadDTO.setConnectAddress(documentVO.getDocumentId());

		fileService.uploadFile(files, uploadDTO);
		
		System.out.print(documentVO.getDocumentId() + "문서 등록");
		return "redirect:/project/" + projectId +"/document/detail/" + documentId;
    }
	
	@GetMapping("/historylist/{documentId}")
    public String documentHistoryList(Model model,@PathVariable("projectId") String projectId, @CookieValue(value="userId", required =false)String userId, @PathVariable String documentId) {
		System.out.println(documentId);
	    List<DocumentHistoryVO> historydocList = documentService.selectHistoryAll(documentId);
	    System.out.println(historydocList.size());
	    model.addAttribute("historydocList",historydocList);
        return "document/documentHistoryList";
    }

	
	@GetMapping("/historydetail/{documentHistoryId}")
    public String documenthistoryDetail(Model model,@PathVariable("projectId") String projectId,@CookieValue(value="userId", required =false)String userId, @PathVariable String documentHistoryId) {
		
		System.out.println(documentHistoryId);
	    DocumentVO docDetail = documentService.selectHistoryDetail(documentHistoryId);
	    model.addAttribute("docDetail",docDetail);
	    List<FileVO> fileList = fileService.selectAll(documentHistoryId);
	    model.addAttribute("fileList",fileList);
        return "document/documentHistoryDetail";
    }
	
	@GetMapping("/addcategory")
    public String documentAddCategory(@PathVariable("projectId") String projectId) {
		
        return "document/documentList";
    }
	
	@PostMapping("/addcategory")
    public String documentAddCategoryProc(DocumentCategoryVO documentCategoryVO, @PathVariable("projectId") String projectId) {
		
		
		logger.debug(documentCategoryVO.toString());
		documentService.insertCategory(documentCategoryVO);
		
        return "redirect:/project/" + projectId +"/document/list";
    }

}
