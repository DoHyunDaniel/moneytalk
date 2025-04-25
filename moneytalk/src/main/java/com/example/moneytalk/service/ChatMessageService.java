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

/**
 * 채팅 메시지의 저장, 조회, 삭제(나가기) 기능을 담당하는 서비스 클래스입니다.
 * 메시지의 soft delete 처리, 읽음 여부, 전송자/수신자 설정 등 도메인 로직을 포함합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	/**
	 * 채팅 메시지를 저장하고 DTO로 반환합니다.
	 * - 채팅방과 사용자 유효성 검사를 수행하며,
	 * - 수신자 정보는 채팅방 내 상대방으로 자동 설정됩니다.
	 * - 채팅방의 마지막 메시지/시간도 함께 갱신됩니다.
	 *
	 * @param dto 저장할 채팅 메시지 DTO
	 * @return 저장된 메시지를 기반으로 생성된 ChatMessageDto
	 * @throws IllegalArgumentException 채팅방 또는 사용자가 존재하지 않을 경우
	 */
	@Transactional
	public ChatMessageDto saveMessage(ChatMessageDto dto) {
	    ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
	            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

	    User sender = userRepository.findById(dto.getSenderId())
	            .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

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

	/**
	 * 특정 채팅방의 메시지 목록을 조회합니다.
	 * - 현재 로그인한 사용자가 채팅방의 참여자인지 검증하고,
	 * - 해당 사용자의 soft delete 상태에 따라 메시지를 필터링합니다.
	 *
	 * @param chatRoomId 채팅방 ID
	 * @param loginUser 현재 로그인한 사용자
	 * @return 필터링된 메시지 응답 DTO 리스트
	 * @throws IllegalArgumentException 채팅방이 존재하지 않을 경우
	 * @throws SecurityException 사용자가 채팅방 참여자가 아닐 경우
	 */
	@Transactional(readOnly = true)
	public List<ChatMessageResponseDto> getMessagesForChatRoom(Long chatRoomId, User loginUser) {
		log.debug("✅ getMessagesForChatRoom 호출됨 - roomId={}, userId={}", chatRoomId, loginUser.getId());

		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		if (!room.getBuyer().getId().equals(loginUser.getId()) && !room.getSeller().getId().equals(loginUser.getId())) {
	        log.warn("⛔️ 접근 권한 없음: userId={}는 이 채팅방에 속하지 않음", loginUser.getId());
			throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
		}

		List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

		return messages.stream().filter(msg -> {
			if (loginUser.equals(msg.getSender())) {
				return !msg.isDeletedBySender();
			} else {
				return !msg.isDeletedByReceiver();
			}
		}).map(ChatMessageResponseDto::from).toList();
	}

	/**
	 * 사용자가 특정 채팅방에서 나가는 로직입니다.
	 * - 본인의 메시지는 `isDeletedBySender`로,
	 * - 상대방의 메시지는 `isDeletedByReceiver`로 soft delete 처리됩니다.
	 * - 모든 메시지가 삭제되었을 경우 채팅방을 종료 상태로 전환합니다.
	 *
	 * @param chatRoomId 나갈 채팅방 ID
	 * @param loginUser 현재 로그인한 사용자
	 * @throws IllegalArgumentException 채팅방이 존재하지 않을 경우
	 */
	@Transactional
	public void leaveChatRoom(Long chatRoomId, User loginUser) {
		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(chatRoomId);

		for (ChatMessage msg : messages) {
			if (msg.getSender().equals(loginUser)) {
				msg.setDeletedBySender(true);
			} else {
				msg.setDeletedByReceiver(true);
			}
		}

		room.closeRoom(); // 전체 삭제 시 종료 여부는 도메인 정책에 따라 선택 가능
	}

	/**
	 * 사용자가 해당 채팅방에 구독 중인지 확인하는 메소드입니다.
	 * 현재는 더미(true) 값으로 구현되어 있으며, 추후 Redis나 WebSocket 세션 상태 확인 로직으로 대체 가능합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @param userId 사용자 ID
	 * @return 현재는 항상 true 반환
	 */
	public boolean isUserSubscribedToRoom(Long roomId, Long userId) {
		return true;
	}

	/**
	 * ChatMessage 엔티티를 ChatMessageDto로 변환합니다.
	 *
	 * @param message 변환할 메시지 엔티티
	 * @return DTO 형태로 변환된 메시지
	 */
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
