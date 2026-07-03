package com.pixcel.app.wiki.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.pixcel.app.wiki.service.WikiPageVO;
import com.pixcel.app.wiki.service.WikiVersionVO;

@Mapper
public interface WikiMapper {
    // 위키 페이지 조회
    public WikiPageVO selectWikiPage(String wikiId);
    // 위키 페이지 등록
    public void insertWikiPage(WikiPageVO vo);
    // 위키 버전 등록
    public void insertWikiVersion(WikiVersionVO vo);
    // 위키 최신 버전 내용 조회
    public WikiVersionVO selectLatestVersion(String wikiId);
    public List<WikiVersionVO> selectVersionList(String wikiId);
    public WikiVersionVO selectVersionDetail(String versionId);
    public List<WikiPageVO> selectWikiList(String projectId);
    public WikiPageVO selectWikiByTitle(@Param("title") String title, @Param("projectId") String projectId);
}