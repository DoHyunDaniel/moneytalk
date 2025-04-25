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
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

	    // ✅ 수신자 결정: sender가 buyer면 수신자는 seller, 그 반대도 마찬가지
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
	            .sentAt(LocalDateTime.now())
	            .build();

	    chatRoom.setLastMessage(dto.getMessage() != null ? dto.getMessage() : "[이미지]");
	    chatRoom.setLastMessageAt(LocalDateTime.now());

	    chatMessageRepository.save(message);

	    return toDto(message);
	}


	@Transactional(readOnly = true)
	public List<ChatMessageResponseDto> getMessagesForChatRoom(Long chatRoomId, User loginUser) {
		log.debug("✅ getMessagesForChatRoom 호출됨 - roomId={}, userId={}", chatRoomId, loginUser.getId());

		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 사용자 권한 체크
		if (!room.getBuyer().getId().equals(loginUser.getId()) && !room.getSeller().getId().equals(loginUser.getId())) {
	        log.warn("⛔️ 접근 권한 없음: userId={}는 이 채팅방에 속하지 않음", loginUser.getId());
			throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
		}

		// soft delete 필터링
		List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

		return messages.stream().filter(msg -> {
			if (loginUser.equals(msg.getSender())) {
				return !msg.isDeletedBySender();
			} else {
				return !msg.isDeletedByReceiver();
			}
		}).map(ChatMessageResponseDto::from).toList();
	}

	@Transactional
	public void leaveChatRoom(Long chatRoomId, User loginUser) {
		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		// 메시지 목록 가져오기
		List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

		for (ChatMessage msg : messages) {
			if (msg.getSender().equals(loginUser)) {
				msg.setDeletedBySender(true);
			} else {
				msg.setDeletedByReceiver(true);
			}
		}

		// 만약 모든 메시지가 삭제되면 room 종료 (선택적으로 처리 가능)
		room.closeRoom();
	}

	public boolean isUserSubscribedToRoom(Long roomId, Long userId) {
		// 실제로는 Redis나 WebSocketSessionManager 등으로 구독 여부를 확인할 수 있음
		// 일단은 true 반환 (구현 예정)
		return true;
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
