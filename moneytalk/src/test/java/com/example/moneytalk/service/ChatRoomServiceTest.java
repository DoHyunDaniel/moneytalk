package com.example.moneytalk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.moneytalk.domain.ChatMessage;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ChatMessageRepository;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.type.ErrorCode;

class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private RedisSubscriberService redisSubscriberService;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getChatRoomDetail_채팅방없음_예외() {
        // given
        Long roomId = 1L;
        User loginUser = User.builder().id(10L).build();

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        // when & then
        GlobalException ex = assertThrows(GlobalException.class,
            () -> chatRoomService.getChatRoomDetail(roomId, loginUser));
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getChatRoomDetail_참여자아님_예외() {
        // given
        Long roomId = 1L;
        User loginUser = User.builder().id(10L).build(); // 로그인 사용자
        User buyer = User.builder().id(2L).build();
        User seller = User.builder().id(3L).build();
        Product product = Product.builder().id(100L).title("아이폰").build();

        ChatRoom chatRoom = ChatRoom.builder()
            .id(roomId)
            .buyer(buyer)
            .seller(seller)
            .product(product)
            .build();

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        // when & then
        GlobalException ex = assertThrows(GlobalException.class,
            () -> chatRoomService.getChatRoomDetail(roomId, loginUser));
        assertEquals(ErrorCode.CHATROOM_ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getChatRoomDetail_정상조회_성공() {
        // given
        Long roomId = 1L;
        User buyer = User.builder().id(10L).nickname("buyer").profileImageUrl("buyer.png").build();
        User seller = User.builder().id(20L).nickname("seller").profileImageUrl("seller.png").build();
        Product product = Product.builder().id(100L).title("맥북").build();

        ChatRoom room = ChatRoom.builder()
                .id(roomId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .build();

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(room));

        // when
        var result = chatRoomService.getChatRoomDetail(roomId, buyer); // 로그인 유저가 buyer

        // then
        assertEquals(roomId, result.getChatRoomId());
        assertEquals(100L, result.getProductId());
        assertEquals("맥북", result.getProductTitle());
        assertEquals("seller", result.getOpponentNickname());
        assertEquals("seller.png", result.getOpponentProfileImage());
    }
    @Test
    void createChatRoom_존재하면재사용() {
        // given
        Product product = Product.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();
        User seller = User.builder().id(3L).build();
        ChatRoom existingRoom = ChatRoom.builder().id(100L).product(product).buyer(buyer).seller(seller).build();

        given(chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller))
                .willReturn(Optional.of(existingRoom));

        // when
        ChatRoom result = chatRoomService.createChatRoom(product, buyer, seller);

        // then
        assertEquals(100L, result.getId());
        verify(chatRoomRepository, never()).save(any()); // 저장 안 함
        verify(redisSubscriberService, never()).subscribeChatRoom(any()); // 구독도 안 함
    }
    @Test
    void createChatRoom_없으면생성() {
        // given
        Product product = Product.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();
        User seller = User.builder().id(3L).build();
        ChatRoom newRoom = ChatRoom.builder().id(200L).product(product).buyer(buyer).seller(seller).build();

        given(chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller))
                .willReturn(Optional.empty());
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(newRoom);

        // when
        ChatRoom result = chatRoomService.createChatRoom(product, buyer, seller);

        // then
        assertEquals(200L, result.getId());
        verify(chatRoomRepository).save(any());
        verify(redisSubscriberService).subscribeChatRoom(200L);
    }
    @Test
    void getChatRoomsForUser_정상조회() {
        // given
        User buyer = User.builder().id(1L).nickname("buyer").profileImageUrl("buyer.png").build();
        User seller = User.builder().id(2L).nickname("seller").profileImageUrl("seller.png").build();
        Product product = Product.builder().id(100L).title("아이폰").images(new ArrayList<>()).build();

        ChatRoom room = ChatRoom.builder()
                .id(10L)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .lastMessage("안녕하세요")
                .lastMessageAt(LocalDateTime.now())
                .build();

        given(chatRoomRepository.findByBuyerOrSellerOrderByLastMessageAtDesc(buyer, buyer))
                .willReturn(List.of(room));
        given(chatMessageRepository.countByChatRoomAndSenderNotAndIsReadFalse(room, buyer))
                .willReturn(2);

        // when
        var result = chatRoomService.getChatRoomsForUser(buyer);

        // then
        assertEquals(1, result.size());
        var dto = result.get(0);
        assertEquals("seller", dto.getOpponentNickname());
        assertEquals(2, dto.getUnreadCount());
    }
    @Test
    void markMessagesAsRead_정상호출() {
        // given
        Long roomId = 1L;
        Long userId = 10L;

        ChatMessage msg1 = ChatMessage.builder().id(1L).isRead(false).build();
        ChatMessage msg2 = ChatMessage.builder().id(2L).isRead(false).build();

        given(chatMessageRepository.findByChatRoomIdAndReceiverIdAndIsReadFalse(roomId, userId))
                .willReturn(List.of(msg1, msg2));

        // when
        chatRoomService.markMessagesAsRead(roomId, userId);

        // then
        assertTrue(msg1.isRead());
        assertTrue(msg2.isRead());
    }


}
