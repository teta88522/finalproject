package com.pixcel.app.wiki.service;

import lombok.Data;
import java.util.Date;

@Data
public class WikiPageVO {
    private String wikiId;
    private String projectId;
    private String parentWikiId;
    private String title;
    private String summary;
    private String currentVersionNo;
    private String editAvailableYn;
    private String editingUserId;
    private Date editingStartedAt;
    private Date editingExpiresAt;
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
}