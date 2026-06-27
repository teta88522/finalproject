package com.pixcel.app.wiki.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pixcel.app.wiki.service.WikiPageVO;
import com.pixcel.app.wiki.service.WikiVersionVO;

@Mapper
public interface WikiMapper {
    // 위키 페이지 조회
    WikiPageVO selectWikiPage(String wikiId);
    // 위키 페이지 등록
    void insertWikiPage(WikiPageVO vo);
    // 위키 버전 등록
    void insertWikiVersion(WikiVersionVO vo);
    // 위키 최신 버전 내용 조회
    WikiVersionVO selectLatestVersion(String wikiId);
    List<WikiVersionVO> selectVersionList(String wikiId);
    WikiVersionVO selectVersionDetail(String versionId);
    List<WikiPageVO> selectWikiList(String projectId);
}