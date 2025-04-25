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

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public ChatRoom createChatRoom(Product product, User buyer, User seller) {
		return chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller)
				.orElseGet(() -> chatRoomRepository
						.save(ChatRoom.builder().product(product).buyer(buyer).seller(seller).isClosed(false).build()));
	}

	/**
	 * 현재 로그인한 사용자가 참여 중인 채팅방 요약 리스트를 반환합니다.
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomSummaryDto> getChatRoomsForUser(User loginUser) {
		return chatRoomRepository.findByBuyerOrSellerOrderByLastMessageAtDesc(loginUser, loginUser).stream()
				.map(room -> {
					User opponent = resolveOpponent(room, loginUser);
					int unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndIsReadFalse(room, loginUser);

					return ChatRoomSummaryDto.builder().chatRoomId(room.getId()).productId(room.getProduct().getId())
							.productTitle(room.getProduct().getTitle())
							.productThumbnailUrl(room.getProduct().getThumbnailUrl())
							.opponentNickname(opponent.getNickname())
							.opponentProfileImage(opponent.getProfileImageUrl()).lastMessage(room.getLastMessage())
							.lastMessageAt(room.getLastMessageAt()).unreadCount(unreadCount).build();
				}).toList();
	}

	@Transactional(readOnly = true)
	public Optional<ChatRoom> getChatRoom(Product product, User buyer, User seller) {
		return chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller);
	}

	/**
	 * 현재 사용자가 속한 채팅방에서 상대방 정보를 찾아 반환합니다.
	 */
	private User resolveOpponent(ChatRoom room, User loginUser) {
		return room.getBuyer().equals(loginUser) ? room.getSeller() : room.getBuyer();
	}
	
	
	@Transactional(readOnly = true)
	public ChatRoomDetailDto getChatRoomDetail(Long roomId, User loginUser) {
	    ChatRoom room = chatRoomRepository.findById(roomId)
	        .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

	    if (!room.getBuyer().equals(loginUser) && !room.getSeller().equals(loginUser)) {
	        throw new SecurityException("접근 권한이 없습니다.");
	    }

	    User opponent = room.getBuyer().equals(loginUser) ? room.getSeller() : room.getBuyer();

	    return ChatRoomDetailDto.builder()
	        .chatRoomId(room.getId())
	        .productId(room.getProduct().getId())
	        .productTitle(room.getProduct().getTitle())
	        .opponentNickname(opponent.getNickname())
	        .opponentProfileImage(opponent.getProfileImageUrl())
	        .build();
	}
	
	@Transactional
	public void markMessagesAsRead(Long chatRoomId, Long userId) {
	    List<ChatMessage> unreadMessages = chatMessageRepository
	        .findByChatRoomIdAndReceiverIdAndIsReadFalse(chatRoomId, userId);

	    for (ChatMessage msg : unreadMessages) {
	        msg.setRead(true);
	    }
	}


}
