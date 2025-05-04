package com.example.moneytalk.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.moneytalk.type.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@JsonIgnore
	@Column(nullable = false)
	private String password;

	@Column(nullable = false, unique = true, length = 30)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private UserType role = UserType.USER;

	@CreationTimestamp
	private LocalDateTime createdAt;

	// 디폴트 이미지 적용
	@Column(nullable = true)
	private String profileImageUrl;


	// 1. Spring Security 권한 정보 반환
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
	}

	// 2. 로그인 식별자 (username) → 이메일 사용
	@Override
	public String getUsername() {
		return this.email;
	}

	// 3. 계정 상태 기본 설정 (모두 true)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// 양방향 매핑 추가
	@OneToMany(mappedBy = "seller")
	private List<ChatRoom> sellChatRooms = new ArrayList<>();

	@OneToMany(mappedBy = "buyer")
	private List<ChatRoom> buyChatRooms = new ArrayList<>();

}
