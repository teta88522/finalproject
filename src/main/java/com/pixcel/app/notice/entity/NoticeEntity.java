package com.pixcel.app.notice.entity;

import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name="BOARD")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NoticeEntity {
	@Id
	@Column(name = "BOARD_ID", length = 255 )
	private String boardId;
	
	@Column(name = "PROJECT_ID", nullable = false)
	private String projectId;
	
	@Column(name = "BOARD_NAME", nullable = false , length = 255)
	private String boardName;
	
	@CreationTimestamp
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDate createdAt;
	
	@Column(name = "CREATED_BY", nullable = false, updatable = false)
    private String createdBy;
	
	@Column(name = "DESCRIPTION", length = 1000)
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", referencedColumnName = "USER_ID", insertable = false, updatable = false)
    private UserEntity user;
}
