package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.type.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 메시지 조회 응답 DTO입니다.
 * 단일 메시지의 상세 정보를 포함하며, 메시지 ID, 보낸 사람 정보, 메시지 타입, 읽음 여부 등을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "채팅 메시지 조회 응답 DTO")
public class ChatMessageResponseDto {

    @Schema(description = "메시지 ID", example = "12345", required = true)
    private Long messageId;

    @Schema(description = "보낸 사람의 ID", example = "7", required = true)
    private Long senderId;

    @Schema(description = "보낸 사람 닉네임", example = "dohyunnn", required = true)
    private String senderNickname;

    @Schema(description = "보낸 사람 프로필 이미지 URL", example = "https://moneytalk.s3.ap-northeast-2.amazonaws.com/profile.jpg")
    private String senderProfileImage;

    @Schema(description = "텍스트 메시지 내용", example = "안녕하세요! 오늘 오후에 가능할까요?")
    private String message;

    @Schema(description = "이미지 메시지일 경우의 URL", example = "https://moneytalk.s3.ap-northeast-2.amazonaws.com/chat-images/sample.jpg")
    private String imageUrl;

    @Schema(description = "메시지 타입 (TEXT, IMAGE, SYSTEM)", example = "TEXT")
    private MessageType type;

    @Schema(description = "메시지 전송 시간", example = "2025-04-25T15:30:00")
    private LocalDateTime sentAt;

    @Schema(description = "해당 메시지를 수신자가 읽었는지 여부", example = "true")
    private boolean isRead;

    /**
     * ChatMessage 엔티티를 ChatMessageResponseDto로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param message ChatMessage 엔티티
     * @return 변환된 ChatMessageResponseDto
     */
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
