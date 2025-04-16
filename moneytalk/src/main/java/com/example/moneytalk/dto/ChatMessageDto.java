package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.type.MessageType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto {

    private Long chatRoomId;           // 채팅방 ID
    private Long senderId;             // 보낸 사람 ID
    private String senderNickname;     // 보낸 사람 닉네임
    private String message;            // 메시지 본문
    private MessageType type;          // TEXT, IMAGE, SYSTEM 등

    private String imageUrl;           // 이미지 메시지일 경우 해당 URL (nullable)
    private LocalDateTime sentAt;      // 메시지 전송 시간 (응답 시 클라이언트 표시용)
}
