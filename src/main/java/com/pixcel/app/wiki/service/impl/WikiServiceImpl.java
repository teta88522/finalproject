package com.pixcel.app.wiki.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixcel.app.wiki.mapper.WikiMapper;
import com.pixcel.app.wiki.service.WikiPageVO;
import com.pixcel.app.wiki.service.WikiService;
import com.pixcel.app.wiki.service.WikiVersionVO;

@Service
public class WikiServiceImpl implements WikiService {

    @Autowired
    private WikiMapper wikiMapper;

    @Override
    public WikiPageVO getWikiPage(String wikiId) {
        return wikiMapper.selectWikiPage(wikiId);
    }

    @Override
    public void saveVersion(WikiVersionVO vo) {
        wikiMapper.insertWikiVersion(vo);
    }

    @Override
    public WikiVersionVO getLatestVersion(String wikiId) {
        return wikiMapper.selectLatestVersion(wikiId);
    }

	@Override
	public void insertWikiPage(WikiPageVO vo) {
		wikiMapper.insertWikiPage(vo);
		
	}

	@Override
	public List<WikiVersionVO> getVersionList(String wikiId) {
		return wikiMapper.selectVersionList(wikiId);
	}

	@Override
	public WikiVersionVO getVersionDetail(String versionId) {
		return wikiMapper.selectVersionDetail(versionId);
	}

	@Override
	public List<WikiPageVO> getWikiList(String projectId) {
		return wikiMapper.selectWikiList(projectId);
	}
}
