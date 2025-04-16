package com.example.moneytalk.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDto saveMessage(ChatMessageDto dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        User sender = userRepository.findById(dto.getSenderId())
            .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        ChatMessage message = ChatMessage.builder()
            .chatRoom(chatRoom)
            .sender(sender)
            .message(dto.getMessage())
            .type(dto.getType())
            .imageUrl(dto.getImageUrl())
            .isRead(false)
            .isDeletedBySender(false)
            .isDeletedByReceiver(false)
            .build();

        // 최신 메시지 정보 반영 (optional)
        // 메세지가 비어있고 이미지만 있을 때 UI용 디폴트 메세지 표시
        chatRoom.setLastMessage(dto.getMessage() != null ? dto.getMessage() : "[이미지]");
        
        // 채팅방 목록에서 최신 메세지 보여주기
        chatRoom.setLastMessageAt(LocalDateTime.now());

        chatMessageRepository.save(message);

        return toDto(message);
    }

    private ChatMessageDto toDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderNickname(message.getSender().getNickname());
        dto.setMessage(message.getMessage());
        dto.setImageUrl(message.getImageUrl());
        dto.setSentAt(message.getSentAt());
        dto.setType(message.getType());
        return dto;
    }
}
