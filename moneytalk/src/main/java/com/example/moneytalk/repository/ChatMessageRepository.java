package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.User;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	/**
	 * 특정 채팅방에서 상대방이 보낸 안 읽은 메시지 수를 반환합니다.
	 *
	 * @param room 대상 채팅방
	 * @param user 현재 로그인한 사용자 (메시지를 읽는 사용자)
	 * @return 안 읽은 메시지 수
	 */
	@Query("""
	    SELECT COUNT(m) FROM ChatMessage m
	    WHERE m.chatRoom = :room
	      AND m.sender <> :user
	      AND m.isRead = false
	""")
	int countUnreadMessages(@Param("room") ChatRoom room, @Param("user") User user);

	
	/**
	 * 특정 채팅방 ID와 사용자 ID를 기반으로 안 읽은 메시지 수를 반환합니다.
	 * sender.id가 userId와 다른 메시지들만 카운트합니다.
	 *
	 * @param chatRoomId 채팅방 ID
	 * @param userId 현재 사용자 ID
	 * @return 안 읽은 메시지 수
	 */
	@Query("""
	    SELECT COUNT(cm)
	    FROM ChatMessage cm
	    WHERE cm.chatRoom.id = :chatRoomId
	      AND cm.isRead = false
	      AND cm.sender.id <> :userId
	""")
	long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

	
	/**
	 * Query Method 버전으로, 특정 채팅방에서 내가 아닌 사용자가 보낸 안 읽은 메시지 수를 반환합니다.
	 *
	 * @param room 대상 채팅방
	 * @param currentUser 현재 로그인한 사용자 (보낸 사람이 아닌 기준)
	 * @return 안 읽은 메시지 수
	 */
	int countByChatRoomAndSenderNotAndIsReadFalse(ChatRoom room, User currentUser);

	
	/**
	 * 삭제되지 않은 채팅 메시지를 채팅방 ID 기준으로 모두 조회합니다.
	 * 채팅방 내 메시지 리스트를 조회할 때 사용됩니다.
	 *
	 * @param chatRoomId 채팅방 ID
	 * @return 삭제되지 않은 메시지 리스트
	 */
	List<ChatMessage> findByChatRoomIdAndIsDeletedBySenderFalse(Long chatRoomId);

	List<ChatMessage> findByChatRoomIdAndReceiverIdAndIsReadFalse(Long chatRoomId, Long receiverId);


}
