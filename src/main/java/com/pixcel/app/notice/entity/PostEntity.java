package com.pixcel.app.notice.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POST")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PostEntity implements Persistable<String> {
	@Id
	@Column(name = "POST_ID")
	private String postId;
	
	@Column(name = "BOARD_ID")
	private String boardId;
	
	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "CONTENT")
	private String content;
	
	@Column(name = "VIEW_COUNT")
	private Integer viewCount;
	
	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@CreatedDate
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;
	
	@Column(name = "CREATE_By")
	private String createBy;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOARD_ID", insertable = false, updatable = false)
    private NoticeEntity notice;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", referencedColumnName = "USER_ID", insertable = false, updatable = false)
    private UserEntity user;

	@Override
    public String getId() {
        return this.postId;
    }

	
    @Override
    public boolean isNew() {
        return true; 
    }

	public void increaseViewCount() {
        this.viewCount = (this.viewCount != null ? this.viewCount : 0) + 1;
    }

	public void updatePost(String title, String content, String modifierId) {
        this.title = title;
        this.content = content;
        this.createBy = modifierId;
        this.updatedAt = LocalDateTime.now();
    }
}
