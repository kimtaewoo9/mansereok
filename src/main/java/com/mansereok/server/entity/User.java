package com.mansereok.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username; // 사용자 아이디
	private String name; // 사용자 본명
	private String password;
	private String email;
	@Enumerated(EnumType.STRING)
	private Role role = Role.USER;
	private boolean enabled = true; // 계정 활성화 상태 .
	// Oauth
	private SocialType socialType;
	private String socialId;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public User(String username, String password, String email, Role role, boolean enabled) {
		this.username = username;
		this.password = password;
		this.email = email; // 이메일 정보 강제 ..해야함
		this.role = role;
		this.enabled = enabled;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public static User create(String username, String name, String email, String socialId,
		SocialType socialType) {
		User user = new User();
		user.username = username; // 아이디
		user.name = name; // 이름
		user.email = email;
		user.createdAt = LocalDateTime.now();
		user.updatedAt = LocalDateTime.now();
		user.socialId = socialId;
		user.socialType = socialType;
		return user;
	}

	@PreUpdate // 엔티티 업데이트 될때마다 자동으로 updateAt 필드를 현재시간으로 설정 .
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
