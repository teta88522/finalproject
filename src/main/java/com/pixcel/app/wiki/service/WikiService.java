package com.pixcel.app.wiki.service;

import java.util.List;

public interface WikiService {
	List<WikiPageVO> getWikiList(String projectId);
    WikiPageVO getWikiPage(String wikiId);
    void saveVersion(WikiVersionVO vo);
    WikiVersionVO getLatestVersion(String wikiId);
    void insertWikiPage(WikiPageVO vo);
    List<WikiVersionVO> getVersionList(String wikiId);
    WikiVersionVO getVersionDetail(String versionId);
}