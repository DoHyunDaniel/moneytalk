package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.ChatRoom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponseDto {

    private Long chatRoomId;
    private Long productId;
    private String buyerNickname;
    private String sellerNickname;
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    public static ChatRoomResponseDto from(ChatRoom room) {
        return ChatRoomResponseDto.builder()
            .chatRoomId(room.getId())
            .productId(room.getProduct().getId())
            .buyerNickname(room.getBuyer().getNickname())
            .sellerNickname(room.getSeller().getNickname())
            .lastMessage(room.getLastMessage())
            .lastMessageAt(room.getLastMessageAt())
            .build();
    }
}
