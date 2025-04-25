package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatMessageResponseDto;
import com.example.moneytalk.dto.ChatRoomDetailDto;
import com.example.moneytalk.dto.ChatRoomResponseDto;
import com.example.moneytalk.dto.ChatRoomSummaryDto;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.service.ChatMessageService;
import com.example.moneytalk.service.ChatRoomService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ProductRepository productRepository;
    private final ChatMessageService chatMessageService;

    @PostMapping("/{productId}")
    @Operation(summary = "채팅방 생성", description = "상품 ID를 기준으로 판매자와 구매자 간의 채팅방을 생성합니다.")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal User loginUser
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomService.createChatRoom(
                product,
                loginUser,
                product.getUser() // 판매자
        );

        return ResponseEntity.ok(ChatRoomResponseDto.from(chatRoom));
    }

    @GetMapping
    @Operation(summary = "채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomSummaryDto>> getMyChatRooms(
            @AuthenticationPrincipal User loginUser
    ) {
        List<ChatRoomSummaryDto> rooms = chatRoomService.getChatRoomsForUser(loginUser);
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 전체를 조회합니다.")
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatMessages(
        @PathVariable("chatRoomId") Long chatRoomId,
        @AuthenticationPrincipal User loginUser) {

        if (loginUser == null) {
            System.out.println("❌ [ERROR] 로그인 유저가 null입니다.");
        } else {
            System.out.println("👤 로그인 유저: ID=" + loginUser.getId() + ", Email=" + loginUser.getEmail());
        }

        List<ChatMessageResponseDto> messages = chatMessageService.getMessagesForChatRoom(chatRoomId, loginUser);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "채팅방 나가기", description = "사용자가 특정 채팅방에서 나가며 자신의 메시지만 soft delete 처리합니다.")
    @DeleteMapping("/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
        @PathVariable("chatRoomId") Long chatRoomId,
        @AuthenticationPrincipal User loginUser) {

        chatMessageService.leaveChatRoom(chatRoomId, loginUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/detail")
    public ResponseEntity<ChatRoomDetailDto> getChatRoomDetail(
        @PathVariable("roomId") Long roomId,
        @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(chatRoomService.getChatRoomDetail(roomId, loginUser));
    }
    
    @Operation(summary = "메시지 읽음 처리", description = "현재 로그인한 사용자가 해당 채팅방의 메시지를 읽음 처리합니다.")
    @PatchMapping("/{chatRoomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable("chatRoomId") Long chatRoomId,
            @AuthenticationPrincipal User loginUser
    ) {
        chatRoomService.markMessagesAsRead(chatRoomId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }


}