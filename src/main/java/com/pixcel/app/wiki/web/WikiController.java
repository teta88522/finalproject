package com.pixcel.app.wiki.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.pixcel.app.file.mapper.FileMapper;
import com.pixcel.app.file.service.FileDTO;
import com.pixcel.app.file.service.FileService;
import com.pixcel.app.file.service.FileVO;
import com.pixcel.app.web.AllProjectController;
import com.pixcel.app.wiki.service.WikiPageVO;
import com.pixcel.app.wiki.service.WikiService;
import com.pixcel.app.wiki.service.WikiVersionVO;

import jakarta.servlet.http.HttpServletResponse;

@AllProjectController
@RequestMapping("/wiki")
@Controller
public class WikiController {

    @Autowired
    private WikiService wikiService;
    
    @Autowired
    private FileService fileService;
    private FileMapper fileMapper;
    // 위키 목록
    @GetMapping("/list")
    public String wikiList(@PathVariable String projectId, Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("wikiList", wikiService.getWikiList(projectId));
        return "wiki/wikiList";
    }
    
 // 위키 목록 JSON API (셀렉트용)
    @GetMapping("/listJson")
    @ResponseBody
    public ResponseEntity<?> getWikiListJson(@PathVariable String projectId) {
        return ResponseEntity.ok(wikiService.getWikiList(projectId));
    }
    
    @GetMapping("/{wikiId}")
    public String wikiView(@PathVariable String projectId,
                           @PathVariable String wikiId,
                           Model model) {
        WikiPageVO page = wikiService.getWikiPage(wikiId);
        // version은 JS에서 API로 가져오므로 제거
        model.addAttribute("projectId", projectId);
        model.addAttribute("wikiId", wikiId);
        model.addAttribute("page", page);
        return "wiki/wikiView";
    }

    // 위키 편집
    @GetMapping("/{wikiId}/edit")
    public String wikiEdit(@PathVariable String projectId,
                           @PathVariable String wikiId,
                           Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("wikiId", wikiId);
        return "wiki/wiki";
    }

    // 저장 API
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveWiki(@RequestBody WikiVersionVO vo, @PathVariable String projectId, @CookieValue(value="userId", required =false)String userId) {
        WikiPageVO page = wikiService.getWikiPage(vo.getWikiId());
        if (page == null) {
            WikiPageVO newPage = new WikiPageVO();
            newPage.setWikiId(vo.getWikiId());
            newPage.setProjectId(projectId);
            newPage.setTitle(vo.getTitle());
            newPage.setCurrentVersionNo("v1");
            newPage.setCreatedBy(userId);
            wikiService.insertWikiPage(newPage);
        }
        vo.setVersionId(UUID.randomUUID().toString());
        vo.setVersionNo("v" + System.currentTimeMillis());
        vo.setCreatedBy(userId);
        wikiService.saveVersion(vo);
        return ResponseEntity.ok("저장 완료");
    }

    // 최신 내용 불러오기
    @GetMapping("/load/{wikiId}")
    @ResponseBody
    public ResponseEntity<?> loadWiki(@PathVariable String wikiId,
                                      @PathVariable String projectId) {
        WikiVersionVO vo = wikiService.getLatestVersion(wikiId);
        return ResponseEntity.ok(vo);
    }

    // 위키 생성
    @PostMapping("/create")
    public String createWiki(WikiPageVO vo,
                                        @PathVariable String projectId, @CookieValue(value="userId", required =false)String userId) {

        vo.setProjectId(projectId);
        vo.setCurrentVersionNo("v1");
        vo.setCreatedBy(userId);
        wikiService.insertWikiPage(vo);
        return "redirect:/project/" + projectId +"/wiki/list";
    }
    
 // JS용 JSON API
    @PostMapping("/createJson")
    @ResponseBody
    public ResponseEntity<?> createWikiJson(@RequestBody WikiPageVO vo,
                                             @PathVariable String projectId,
                                             @CookieValue(value="userId", required=false) String userId) {
        String wikiId = "WIKI_PAGE_"
                      + new java.text.SimpleDateFormat("yyMM").format(new java.util.Date())
                      + "_" + String.format("%04d", (int)(Math.random() * 9999));
        vo.setWikiId(wikiId);
        vo.setProjectId(projectId);
        vo.setCurrentVersionNo("v1");
        vo.setCreatedBy(userId);
        wikiService.insertWikiPage(vo);
        return ResponseEntity.ok(vo);
    }
    
    // 버전 목록
    @GetMapping("/versions/{wikiId}")
    @ResponseBody
    public ResponseEntity<?> getVersionList(@PathVariable String wikiId,
                                            @PathVariable String projectId) {
        return ResponseEntity.ok(wikiService.getVersionList(wikiId));
    }

    // 버전 상세
    @GetMapping("/version/{versionId}")
    @ResponseBody
    public ResponseEntity<?> getVersionDetail(@PathVariable String versionId,
                                              @PathVariable String projectId) {
        return ResponseEntity.ok(wikiService.getVersionDetail(versionId));
    }
    


    @PostMapping("/{wikiId}/image/upload")
    @ResponseBody
    public ResponseEntity<?> uploadWikiImage(@PathVariable String projectId,
                                              @PathVariable String wikiId,
                                              @RequestParam("file") MultipartFile file,
                                              @CookieValue(value = "userId", required = false) String userId) {

        FileDTO uploadDTO = new FileDTO();
        uploadDTO.setProjectId(projectId);
		uploadDTO.setFileCode("f002");
		uploadDTO.setUploadUserId(userId);
		uploadDTO.setConnectAddress(wikiId);

        List<FileVO> saved = fileService.uploadFileAndReturn(List.of(file), uploadDTO);

        if (saved.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "업로드 실패"));
        }

        String fileId = saved.get(0).getFileId();
        String url = "/project/" + projectId + "/wiki/" + wikiId + "/image/" + fileId;
        return ResponseEntity.ok(Map.of("url", url));
    }

    // 이미지 인라인 조회 (마크다운 렌더링용)
    @GetMapping("/{wikiId}/image/{fileId}")
    public void viewWikiImage(@PathVariable String projectId,
                               @PathVariable String wikiId,
                               @PathVariable String fileId,
                               HttpServletResponse response) throws IOException {

        FileVO fileInfo = fileService.getFileById(fileId); // fileMapper 대신 fileService 사용
        if (fileInfo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(fileInfo.getFilePath());
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(file.toPath());
        response.setContentType(contentType != null ? contentType : "application/octet-stream");

        try (InputStream is = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) os.write(buffer, 0, length);
            os.flush();
        }
    }
    
 // 제목으로 위키 검색
    @GetMapping("/find")
    @ResponseBody
    public ResponseEntity<?> findByTitle(@RequestParam String title,
                                         @PathVariable String projectId) {
        WikiPageVO page = wikiService.getWikiByTitle(title, projectId);
        if (page == null) {
            return ResponseEntity.ok(new java.util.HashMap<>());
        }
        return ResponseEntity.ok(page);
    }
    
    
}