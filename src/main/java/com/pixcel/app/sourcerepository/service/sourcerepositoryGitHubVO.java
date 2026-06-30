package com.pixcel.app.sourcerepository.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.annotations.SerializedName;

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
public class sourcerepositoryGitHubVO {
private String sha;
    
    @SerializedName("commit")
    private CommitData commit;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class CommitData {
        private String message;
        
        @SerializedName("author")
        private Author author;
        
        @SerializedName("committer")
        private Author committer;
        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @ToString
        public static class Author {
            private String name;
            private String email;
            private String date;
        }
    }

    /**
     * GitHub 응답 → VO 변환
     */
    public sourcerepositoryVO toSourcerepositoryVO(
            String commitId,
            String projectId,
            String issueId,
            String branchName) {
        return sourcerepositoryVO.builder()
            .commitId(commitId)
            .projectId(projectId)
            .issueId(issueId)
            .branchName(branchName)
            .commitHash(this.sha)
            .commitMessage(this.commit.message)
            .authorName(this.commit.author.name)
            .authorEmail(this.commit.author.email)
            .committedAt(parseGitHubDate(this.commit.committer.date))
            .createdAt(LocalDateTime.now())
            .build();
    }

    private LocalDateTime parseGitHubDate(String githubDate) {
        return LocalDateTime.parse(
            githubDate.replace("Z", "+00:00"),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );
    }
}
