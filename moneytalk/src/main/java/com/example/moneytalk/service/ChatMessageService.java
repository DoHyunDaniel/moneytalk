package com.example.moneytalk.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.dto.ChatMessageResponseDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 저장, 조회, 채팅방 나가기(Soft Delete) 기능을 담당하는 서비스 클래스입니다.
 * 
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 채팅 메시지를 저장합니다. (DB 저장 전용)
     *
     * @param dto 저장할 채팅 메시지 DTO
     * @throws GlobalException 채팅방 또는 발신자 미존재 시 발생
     */
    @Transactional
    public void saveMessage(ChatMessageDto dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new GlobalException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new GlobalException(ErrorCode.SENDER_NOT_FOUND));

        User receiver = chatRoom.getBuyer().equals(sender) ? chatRoom.getSeller() : chatRoom.getBuyer();

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .receiver(receiver)
                .message(dto.getMessage())
                .type(dto.getType())
                .imageUrl(dto.getImageUrl())
                .isRead(false)
                .isDeletedBySender(false)
                .isDeletedByReceiver(false)
                .sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now())
                .build();

        chatMessageRepository.save(message);

        chatRoom.setLastMessage(dto.getMessage() != null ? dto.getMessage() : "[이미지]");
        chatRoom.setLastMessageAt(LocalDateTime.now());
    }

    /**
     * 특정 채팅방의 메시지 목록 조회 (Soft Delete 고려)
     *
     * @param chatRoomId 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return 메시지 응답 DTO 리스트
     * @throws GlobalException 채팅방 미존재 또는 접근 권한 없음
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessagesForChatRoom(Long chatRoomId, User loginUser) {
        log.debug("✅ getMessagesForChatRoom 호출됨 - roomId={}, userId={}", chatRoomId, loginUser.getId());

        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHATROOM_NOT_FOUND));

        if (!room.getBuyer().getId().equals(loginUser.getId()) && !room.getSeller().getId().equals(loginUser.getId())) {
            log.warn("⛔️ 접근 권한 없음: userId={}는 이 채팅방에 속하지 않음", loginUser.getId());
            throw new GlobalException(ErrorCode.CHATROOM_ACCESS_DENIED);
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

        return messages.stream()
                .filter(msg -> {
                    if (loginUser.equals(msg.getSender())) {
                        return !msg.isDeletedBySender();
                    } else {
                        return !msg.isDeletedByReceiver();
                    }
                })
                .map(ChatMessageResponseDto::from)
                .toList();
    }

    /**
     * 채팅방 나가기 (Soft Delete)
     *
     * @param chatRoomId 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @throws GlobalException 채팅방 미존재 시 발생
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, User loginUser) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHATROOM_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

        for (ChatMessage msg : messages) {
            if (msg.getSender().equals(loginUser)) {
                msg.setDeletedBySender(true);
            } else {
                msg.setDeletedByReceiver(true);
            }
        }

        boolean allMessagesDeleted = messages.stream()
                .allMatch(m -> m.isDeletedBySender() && m.isDeletedByReceiver());

        if (allMessagesDeleted) {
            room.closeRoom();
        }
    }

    // =============================
    // 🛠️ Helper Methods
    // =============================

    /**
     * ChatMessage 엔티티를 ChatMessageDto로 변환합니다.
     *
     * @param message 변환할 메시지 엔티티
     * @return DTO 형태로 변환된 메시지
     */
    private ChatMessageDto toDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .message(message.getMessage())
                .imageUrl(message.getImageUrl())
                .sentAt(message.getSentAt())
                .type(message.getType())
                .build();
    }
}


