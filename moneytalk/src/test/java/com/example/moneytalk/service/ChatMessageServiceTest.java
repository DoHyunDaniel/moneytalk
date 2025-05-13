package com.example.moneytalk.service;

import com.example.moneytalk.domain.*;
import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ErrorCode;
import com.example.moneytalk.type.MessageType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ChatMessageServiceTest {

	@InjectMocks
	private ChatMessageService chatMessageService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void saveMessage_채팅방없음_예외() {
		// given
		ChatMessageDto dto = ChatMessageDto.builder().chatRoomId(1L).senderId(10L).message("hello")
				.type(MessageType.TEXT).build();

		given(chatRoomRepository.findById(1L)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class, () -> chatMessageService.saveMessage(dto));
		assertEquals(ErrorCode.CHATROOM_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void saveMessage_송신자없음_예외() {
		// given
		ChatMessageDto dto = ChatMessageDto.builder().chatRoomId(1L).senderId(10L).message("hello")
				.type(MessageType.TEXT).build();

		ChatRoom room = ChatRoom.builder().id(1L).buyer(User.builder().id(2L).build())
				.seller(User.builder().id(3L).build()).build();

		given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
		given(userRepository.findById(10L)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class, () -> chatMessageService.saveMessage(dto));
		assertEquals(ErrorCode.SENDER_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void getMessagesForChatRoom_채팅방없음_예외() {
		// given
		User user = User.builder().id(1L).build();

		given(chatRoomRepository.findById(99L)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> chatMessageService.getMessagesForChatRoom(99L, user));
		assertEquals(ErrorCode.CHATROOM_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void getMessagesForChatRoom_접근권한없음_예외() {
		// given
		User user = User.builder().id(99L).build(); // 제3자
		User buyer = User.builder().id(1L).build();
		User seller = User.builder().id(2L).build();
		ChatRoom room = ChatRoom.builder().id(1L).buyer(buyer).seller(seller).build();

		given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> chatMessageService.getMessagesForChatRoom(1L, user));
		assertEquals(ErrorCode.CHATROOM_ACCESS_DENIED, ex.getErrorCode());
	}

	@Test
	void saveMessage_정상저장() {
		// given
		ChatMessageDto dto = ChatMessageDto.builder().chatRoomId(1L).senderId(10L).message("hello")
				.type(MessageType.TEXT).build();

		User sender = User.builder().id(10L).nickname("dohyunnn").build();
		User buyer = User.builder().id(10L).build();
		User seller = User.builder().id(20L).build();

		ChatRoom room = ChatRoom.builder().id(1L).buyer(buyer).seller(seller).build();

		given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
		given(userRepository.findById(10L)).willReturn(Optional.of(sender));

		// when
		chatMessageService.saveMessage(dto);

		// then
		verify(chatMessageRepository).save(any(ChatMessage.class));
		assertEquals("hello", room.getLastMessage());
		assertNotNull(room.getLastMessageAt());
	}

	@Test
	void getMessagesForChatRoom_정상조회() {
		// given
		User buyer = User.builder().id(1L).nickname("buyer").build();
		User seller = User.builder().id(2L).nickname("seller").build();

		ChatRoom room = ChatRoom.builder().id(10L).buyer(buyer).seller(seller).build();

		ChatMessage msg1 = ChatMessage.builder().id(1L).sender(buyer).receiver(seller).message("Hi")
				.isDeletedBySender(false).isDeletedByReceiver(false).build();
		ChatMessage msg2 = ChatMessage.builder().id(2L).sender(seller).receiver(buyer).message("Hello")
				.isDeletedBySender(false).isDeletedByReceiver(false).build();

		given(chatRoomRepository.findById(10L)).willReturn(Optional.of(room));
		given(chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(10L)).willReturn(List.of(msg1, msg2));

		// when
		var result = chatMessageService.getMessagesForChatRoom(10L, buyer);

		// then
		assertEquals(2, result.size());
		assertEquals("Hi", result.get(0).getMessage());
		assertEquals("Hello", result.get(1).getMessage());
	}

	@Test
	void leaveChatRoom_정상삭제() {
		// given
		Long roomId = 1L;
		User loginUser = User.builder().id(1L).build();
		ChatMessage msg1 = ChatMessage.builder().id(1L).sender(loginUser).build();
		ChatMessage msg2 = ChatMessage.builder().id(2L).sender(User.builder().id(2L).build()).build();

		ChatRoom room = ChatRoom.builder().id(roomId).build();

		given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
		given(chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(roomId)).willReturn(List.of(msg1, msg2));

		// when
		chatMessageService.leaveChatRoom(roomId, loginUser);

		// then
		assertTrue(msg1.isDeletedBySender());
		assertTrue(msg2.isDeletedByReceiver());
		assertFalse(room.isClosed());
	}

	@Test
	void leaveChatRoom_전체삭제시_방종료() {
		// given
		Long roomId = 1L;
		User loginUser = User.builder().id(1L).build();

		// 두 메시지 모두 삭제 처리된 상태
		ChatMessage msg1 = ChatMessage.builder().id(1L).sender(loginUser).isDeletedByReceiver(true).build();
		ChatMessage msg2 = ChatMessage.builder().id(2L).sender(User.builder().id(2L).build()).isDeletedBySender(true)
				.build();

		ChatRoom room = ChatRoom.builder().id(roomId).build();

		given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
		given(chatMessageRepository.findByChatRoomIdAndIsDeletedBySenderFalse(roomId)).willReturn(List.of(msg1, msg2));

		// when
		chatMessageService.leaveChatRoom(roomId, loginUser);

		// then
		assertTrue(room.isClosed());
	}

}
