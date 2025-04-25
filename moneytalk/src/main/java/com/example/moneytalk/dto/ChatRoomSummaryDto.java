package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomSummaryDto {

	private Long chatRoomId;
	private Long productId;
	private String productTitle;
	private String productThumbnailUrl;

	private String opponentNickname;
	private String opponentProfileImage;

	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private boolean isClosed;
	private Long opponentUserId;

	private int unreadCount;
}
