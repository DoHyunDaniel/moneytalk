package com.example.moneytalk.controller;

import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatRoomResponseDto;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.service.ChatRoomService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ProductRepository productRepository;

    @PostMapping("/{productId}")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
        @PathVariable("productId") Long productId,
        @AuthenticationPrincipal User loginUser // JWT 인증 처리 시 등록된 사용자
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
}
