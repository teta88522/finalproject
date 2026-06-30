package com.pixcel.app.notice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="USERS")
@Getter
@NoArgsConstructor
public class UserEntity {
	
	@Id
	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name ="USER_NAME", nullable= false)
	private String userName;
}
