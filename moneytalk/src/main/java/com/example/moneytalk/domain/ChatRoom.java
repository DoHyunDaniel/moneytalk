package com.example.moneytalk.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

	// 중고 거래에서 "예약 취소" 후 다시 대화를 이어가야 할 때,
	// 실수로 종료된 채팅방을 복구하고 싶을 때,
	// 관리자 기능에서 "비활성화된 채팅 복원" 기능이 필요할 때
	private boolean isClosed;

	// 양방향 매핑
	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
	private List<ChatMessage> messages = new ArrayList<>();

	// 정렬 쿼리에서 오류 미리 방지
	@PrePersist
	public void setDefaultLastMessageAt() {
		if (this.lastMessageAt == null) {
			this.lastMessageAt = LocalDateTime.now();
		}
	}

	// 편의 메서드
	public void setBuyer(User buyer) {
		this.buyer = buyer;
		if (!buyer.getBuyChatRooms().contains(this)) {
			buyer.getBuyChatRooms().add(this);
		}
	}

	public void setSeller(User seller) {
		this.seller = seller;
		if (!seller.getSellChatRooms().contains(this)) {
			seller.getSellChatRooms().add(this);
		}
	}

	public void updateLastMessage(String message) {
		this.lastMessage = message;
		this.lastMessageAt = LocalDateTime.now();
	}

	public void closeRoom() {
		this.isClosed = true;
	}
}