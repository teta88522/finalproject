package com.pixcel.app.wiki.service;

import java.util.Date;

import lombok.Data;

@Data
public class WikiVersionVO {
    private String versionId;
    private String wikiId;
    private String versionNo;
    private String title;
    private String content;
    private String changeSummary;
    private String createdBy;
    private Date createdAt;
}