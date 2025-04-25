package com.example.moneytalk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatRoomDetailDto;
import com.example.moneytalk.dto.ChatRoomSummaryDto;
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

/**
 * 채팅방 생성, 조회, 읽음 처리 등의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 상품, 구매자, 판매자 정보를 기반으로 채팅방을 생성합니다.
	 * 동일한 조합의 채팅방이 이미 존재할 경우 해당 채팅방을 반환하고,
	 * 존재하지 않으면 새롭게 생성합니다.
	 *
	 * @param product 채팅 대상 상품
	 * @param buyer 구매자
	 * @param seller 판매자
	 * @return 생성된 또는 기존 채팅방
	 */
	@Transactional
	public ChatRoom createChatRoom(Product product, User buyer, User seller) {
		return chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller)
				.orElseGet(() -> chatRoomRepository
						.save(ChatRoom.builder()
								.product(product)
								.buyer(buyer)
								.seller(seller)
								.isClosed(false)
								.build()));
	}

	/**
	 * 현재 로그인한 사용자가 참여 중인 모든 채팅방 요약 정보를 조회합니다.
	 * 각 채팅방에 대해 상대방 정보, 마지막 메시지, 안 읽은 메시지 수를 포함합니다.
	 *
	 * @param loginUser 현재 로그인한 사용자
	 * @return 채팅방 요약 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomSummaryDto> getChatRoomsForUser(User loginUser) {
		return chatRoomRepository.findByBuyerOrSellerOrderByLastMessageAtDesc(loginUser, loginUser).stream()
				.map(room -> {
					User opponent = resolveOpponent(room, loginUser);
					int unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndIsReadFalse(room, loginUser);

					return ChatRoomSummaryDto.builder()
							.chatRoomId(room.getId())
							.productId(room.getProduct().getId())
							.productTitle(room.getProduct().getTitle())
							.productThumbnailUrl(room.getProduct().getThumbnailUrl())
							.opponentNickname(opponent.getNickname())
							.opponentProfileImage(opponent.getProfileImageUrl())
							.lastMessage(room.getLastMessage())
							.lastMessageAt(room.getLastMessageAt())
							.unreadCount(unreadCount)
							.build();
				}).toList();
	}

	/**
	 * 주어진 상품과 구매자, 판매자 조합에 해당하는 채팅방을 조회합니다.
	 *
	 * @param product 상품
	 * @param buyer 구매자
	 * @param seller 판매자
	 * @return 채팅방 Optional 객체
	 */
	@Transactional(readOnly = true)
	public Optional<ChatRoom> getChatRoom(Product product, User buyer, User seller) {
		return chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller);
	}

	/**
	 * 현재 사용자가 속한 채팅방에서 상대방 정보를 반환합니다.
	 * 로그인한 사용자가 구매자면 판매자를, 판매자면 구매자를 반환합니다.
	 *
	 * @param room 채팅방 객체
	 * @param loginUser 현재 사용자
	 * @return 상대방 사용자 객체
	 */
	private User resolveOpponent(ChatRoom room, User loginUser) {
		return room.getBuyer().equals(loginUser) ? room.getSeller() : room.getBuyer();
	}

	/**
	 * 채팅방의 상세 정보를 조회합니다.
	 * - 채팅방 참여자 검증을 수행하며,
	 * - 상대방 정보와 상품 정보를 포함한 DTO를 반환합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @param loginUser 현재 로그인한 사용자
	 * @return 채팅방 상세 정보 DTO
	 * @throws IllegalArgumentException 채팅방이 존재하지 않는 경우
	 * @throws SecurityException 사용자가 채팅방 참여자가 아닌 경우
	 */
	@Transactional(readOnly = true)
	public ChatRoomDetailDto getChatRoomDetail(Long roomId, User loginUser) {
	    ChatRoom room = chatRoomRepository.findById(roomId)
	        .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

	    if (!room.getBuyer().equals(loginUser) && !room.getSeller().equals(loginUser)) {
	        throw new SecurityException("접근 권한이 없습니다.");
	    }

	    User opponent = resolveOpponent(room, loginUser);

	    return ChatRoomDetailDto.builder()
	        .chatRoomId(room.getId())
	        .productId(room.getProduct().getId())
	        .productTitle(room.getProduct().getTitle())
	        .opponentNickname(opponent.getNickname())
	        .opponentProfileImage(opponent.getProfileImageUrl())
	        .build();
	}

	/**
	 * 해당 채팅방의 수신자(userId)가 아직 읽지 않은 메시지를 모두 읽음 처리합니다.
	 *
	 * @param chatRoomId 채팅방 ID
	 * @param userId 메시지를 읽은 사용자 ID (수신자)
	 */
	@Transactional
	public void markMessagesAsRead(Long chatRoomId, Long userId) {
	    List<ChatMessage> unreadMessages = chatMessageRepository
	        .findByChatRoomIdAndReceiverIdAndIsReadFalse(chatRoomId, userId);

	    for (ChatMessage msg : unreadMessages) {
	        msg.setRead(true);
	    }
	}
}

