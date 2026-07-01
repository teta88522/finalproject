package com.pixcel.app.sourcerepository.service;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class sourcerepositoryVO {
	// PK
    private String commitId;
    
    // FK
    private String projectId;
    private String issueId;
    
    // Commit 정보
    private String branchName;
    private String commitHash;
    private String commitMessage;
    private String authorName;
    private String authorEmail;
    
    // 시간 정보
    private LocalDateTime committedAt;
    private LocalDateTime createdAt;
    
    // LEFT JOIN - ISSUE 정보
    @Builder.Default
    private String issueTitle = "-";
    
    @Builder.Default
    private String issueStatusId = "-";
    
    @Builder.Default
    private Integer progressRate = 0;
}