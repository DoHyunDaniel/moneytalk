package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.type.MessageType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponseDto {
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImage;
    private String message;
    private String imageUrl;
    private MessageType type;
    private LocalDateTime sentAt;
    private boolean isRead;

    public static ChatMessageResponseDto from(ChatMessage message) {
        return ChatMessageResponseDto.builder()
            .messageId(message.getId())
            .senderId(message.getSender().getId())
            .senderNickname(message.getSender().getNickname())
            .senderProfileImage(message.getSender().getProfileImageUrl())
            .message(message.getMessage())
            .imageUrl(message.getImageUrl())
            .type(message.getType())
            .sentAt(message.getSentAt())
            .isRead(message.isRead())
            .build();
    }
}
