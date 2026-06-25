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
import com.pixcel.app.document.service.DocumentService;
import com.pixcel.app.document.service.DocumentVO;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.milestones.service.MilestoneSearchVO;
import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
    public String documentList(Model model, @CookieValue(value="userId", required =false)String userId) {
	    List<DocumentCategoryVO> categoryList = documentService.selectCategoryAll();
	    model.addAttribute("categoryList",categoryList);
	    
	    List<DocumentVO> categoryNoList = documentService.selectNoCategory();
	    model.addAttribute("categoryNoList",categoryNoList);
        return "document/documentList";
    }
	
	@GetMapping("/list/{categoryId}")
    public String documentCategoryList(Model model, @CookieValue(value="userId", required =false)String userId, @PathVariable String categoryId) {
		System.out.println(categoryId);
	    List<DocumentVO> categorydocList = documentService.selectCategorydoc(categoryId);
	    System.out.println(categorydocList.size());
	    model.addAttribute("categorydocList",categorydocList);
        return "document/documentCategoryList";
    }
	
	@GetMapping("/add")
    public String documentAdd(Model model, @CookieValue(value="userId", required =false)String userId) {
		MilestoneSearchVO vo = new MilestoneSearchVO();
		vo.setProjectId("PROJECT_ID_2606_0001");
		List<MilestonesVO> milestoneList = milestonesService.getMilestoneList(vo);

	    model.addAttribute("milestoneList", milestoneList);
	    
	    List<CodeValueVO> codeValueList = codeValueService.getCodeValueListByGroup(userId,"g003");
	    model.addAttribute("codeValueList", codeValueList);
	    
	    List<DocumentCategoryVO> categoryList = documentService.selectCategoryAll();
	    model.addAttribute("categoryList",categoryList);
	    
	    
	    
        return "document/documentAdd";
    }
	
	@PostMapping("/add")
	@Transactional
    public String documentAddProc(@CookieValue(value="userId", required =false)String userId, DocumentVO documentVO,  @RequestParam("files") List<MultipartFile> files) {
		
		
		logger.debug(documentVO.toString());
		System.out.print(documentVO);
		documentVO.setCreatedBy(userId);
		documentService.addDocument(documentVO);
		System.out.println(documentVO.getDocumentId());
		
		
		
		
		FileDTO uploadDTO = new FileDTO();
		uploadDTO.setProjectId("PROJECT_ID_2606_0001");
		uploadDTO.setVersionId(documentVO.getVersionId());
		uploadDTO.setFileCode("f001");
		uploadDTO.setUploadUserId(userId);
		uploadDTO.setConnectAddress(documentVO.getDocumentId());

		fileService.uploadFile(files, uploadDTO);
		
		System.out.print(documentVO.getDocumentId() + "문서 등록");
        return "redirect:/document/list";
    }
	
	@GetMapping("/detail/{documentId}")
    public String documentDetail(Model model, @CookieValue(value="userId", required =false)String userId, @PathVariable String documentId) {
		
		System.out.println(documentId);
	    DocumentVO docDetail = documentService.selectDetail(documentId);
	    model.addAttribute("docDetail",docDetail);
	    List<FileVO> fileList = fileService.selectAll(documentId);
	    model.addAttribute("fileList",fileList);
        return "document/documentDetail";
    }
	
	@GetMapping("/detail/{documentId}/download")
	public void downloadFileAll(@PathVariable String documentId,HttpServletResponse response, @CookieValue(value="userId", required =false)String userId) throws IOException{
		fileService.downloadAll(documentId, response, userId);
	}
	
	@GetMapping("/detail/{documentId}/{fileId}/download")
	public void downloadFile(@PathVariable String fileId,HttpServletResponse response, @CookieValue(value="userId", required =false)String userId) throws IOException{
		fileService.downloadOne(fileId, response, userId);
	}
	
	
	@GetMapping("/update")
    public String documentUpdate() {
        return "document/documentUpdate";
    }
	
	@GetMapping("/history")
    public String documentHistory() {
        return "document/documentHistory";
    }
	
	@GetMapping("/addcategory")
    public String documentAddCategory() {
		
        return "document/documentList";
    }
	
	@PostMapping("/addcategory")
    public String documentAddCategoryProc(DocumentCategoryVO documentCategoryVO) {
		
		
		logger.debug(documentCategoryVO.toString());
		documentService.insertCategory(documentCategoryVO);
		
        return "redirect:/document/list";
    }

}
