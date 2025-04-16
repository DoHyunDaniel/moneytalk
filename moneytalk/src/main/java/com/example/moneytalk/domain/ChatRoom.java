package com.example.moneytalk.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	@CreationTimestamp
	private LocalDateTime createdAt;

	// 채팅방 확장 필드
	private String lastMessage;

	private LocalDateTime lastMessageAt;

	private boolean isClosed;

	// 양방향 매핑
	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
	private List<ChatMessage> messages;


	// 정렬 쿼리에서 오류 미리 방지
	@PrePersist
	public void setDefaultLastMessageAt() {
		if (this.lastMessageAt == null) {
			this.lastMessageAt = LocalDateTime.now();
		}
	}
}
