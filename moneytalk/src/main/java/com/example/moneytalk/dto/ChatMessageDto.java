package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.type.MessageType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 채팅 메시지를 송수신할 때 사용되는 DTO입니다.
 * WebSocket 통신 시 주로 사용되며, 텍스트/이미지/SYSTEM 메시지를 포함할 수 있습니다.
 */
@Getter
@Builder
@Schema(description = "채팅 메시지 송수신 DTO")
public class ChatMessageDto {

    @Schema(description = "채팅방 ID", example = "101", required = true)
    private final Long chatRoomId;

    @Schema(description = "보낸 사람의 사용자 ID", example = "5", required = true)
    private final Long senderId;

    @Schema(description = "보낸 사람의 닉네임", example = "dohyunnn", required = true)
    private final String senderNickname;

    @Schema(description = "메시지 본문 내용", example = "안녕하세요! 거래 가능할까요?")
    private final String message;

    @Schema(
        description = "메시지 타입 (TEXT, IMAGE, SYSTEM 중 하나)",
        example = "TEXT",
        required = true,
        implementation = MessageType.class
    )
    private final MessageType type;

    @Schema(description = "이미지 메시지일 경우 이미지 URL", example = "https://moneytalk-bucket.s3.ap-northeast-2.amazonaws.com/chat-images/sample.jpg")
    private final String imageUrl;

    @Schema(description = "메시지 전송 시간", example = "2025-04-25T14:05:00")
    private final LocalDateTime sentAt;
    

}
