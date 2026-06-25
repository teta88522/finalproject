package com.pixcel.app.document.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
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
import com.pixcel.app.milestones.service.MilestoneSearchVO;
import com.pixcel.app.milestones.service.MilestonesService;
import com.pixcel.app.milestones.service.MilestonesVO;

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
    public String documentAddProc(@CookieValue(value="userId", required =false)String userId, DocumentVO documentVO,  @RequestParam("files") List<MultipartFile> files) {
		
		FileDTO fileDTO = new FileDTO();
		
		logger.debug(documentVO.toString());
		System.out.print(documentVO);
		documentVO.setCreatedBy(userId);
		documentService.addDocument(documentVO);
		System.out.println(documentVO.getDocumentId());
		
		fileDTO.setProjectId(documentVO.getProjectId());
		fileDTO.setVersionId(documentVO.getVersionId());
		fileDTO.setUploadUserId(documentVO.getCreatedBy());
		fileDTO.setConnectAddress(documentVO.getDocumentId());

		fileService.uploadFile(files, fileDTO);
		
		System.out.print(documentVO.getDocumentId() + "문서 등록");
        return "redirect:/document/list";
    }
	
	@GetMapping("/detail/{documentId}")
    public String documentDetail(Model model, @CookieValue(value="userId", required =false)String userId, @PathVariable String documentId) {
		
		System.out.println(documentId);
	    DocumentVO docDetail = documentService.selectDetail(documentId);
	    model.addAttribute("docDetail",docDetail);

        return "document/documentDetail";
    }
	
	
	@GetMapping("/update")
    public String documentUpdate() {
        return "document/documentUpdate";
    }
	
	@GetMapping("/history")
    public String documentHistory() {
        return "document/documentHistory";
    }

}
