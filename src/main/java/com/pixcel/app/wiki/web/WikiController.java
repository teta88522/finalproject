package com.pixcel.app.wiki.web;

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
import org.springframework.web.bind.annotation.ResponseBody;
import com.pixcel.app.web.AllProjectController;
import com.pixcel.app.wiki.service.WikiPageVO;
import com.pixcel.app.wiki.service.WikiService;
import com.pixcel.app.wiki.service.WikiVersionVO;

import java.util.List;

@AllProjectController
@RequestMapping("/wiki")
@Controller
public class WikiController {

    @Autowired
    private WikiService wikiService;

    // 위키 목록
    @GetMapping("/list")
    public String wikiList(@PathVariable String projectId, Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("wikiList", wikiService.getWikiList(projectId));
        return "wiki/wikiList";
    }

    // 위키 읽기
    @GetMapping("/{wikiId}")
    public String wikiView(@PathVariable String projectId,
                           @PathVariable String wikiId,
                           Model model) {
        WikiPageVO page = wikiService.getWikiPage(wikiId);
        WikiVersionVO version = wikiService.getLatestVersion(wikiId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("wikiId", wikiId);
        model.addAttribute("page", page);
        model.addAttribute("version", version);
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
    @ResponseBody
    public ResponseEntity<?> createWiki(@RequestBody WikiPageVO vo,
                                        @PathVariable String projectId, @CookieValue(value="userId", required =false)String userId) {
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
}