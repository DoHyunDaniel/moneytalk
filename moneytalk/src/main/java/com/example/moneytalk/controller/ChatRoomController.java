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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 관련 API를 담당하는 컨트롤러입니다.
 * 채팅방 생성, 조회, 나가기 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ProductRepository productRepository;
    private final ChatMessageService chatMessageService;

    /**
     * 상품 ID를 기반으로 채팅방을 생성합니다.
     *
     * @param productId 채팅을 시작할 상품의 ID
     * @param loginUser 현재 로그인한 사용자
     * @return 생성된 채팅방 정보
     */
    @Operation(summary = "채팅방 생성", description = "상품 ID를 기준으로 판매자와 구매자 간의 채팅방을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 생성 성공", content = @Content(schema = @Schema(implementation = ChatRoomResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 상품 ID", content = @Content)
    })
    @PostMapping("/{productId}")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
            @Parameter(description = "채팅을 시작할 상품 ID", example = "1")
            @PathVariable("productId") Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomService.createChatRoom(product, loginUser, product.getUser());

        return ResponseEntity.ok(ChatRoomResponseDto.from(chatRoom));
    }

    /**
     * 로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회합니다.
     *
     * @param loginUser 현재 로그인한 사용자
     * @return 사용자의 채팅방 요약 리스트
     */
    @Operation(summary = "채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ChatRoomSummaryDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<ChatRoomSummaryDto>> getMyChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        List<ChatRoomSummaryDto> rooms = chatRoomService.getChatRoomsForUser(loginUser);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 특정 채팅방의 메시지를 조회합니다.
     *
     * @param chatRoomId 메시지를 조회할 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return 메시지 목록
     */
    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 전체를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ChatMessageResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content)
    })
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatMessages(
        @Parameter(description = "조회할 채팅방 ID", example = "1")
        @PathVariable("chatRoomId") Long chatRoomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessagesForChatRoom(chatRoomId, loginUser);
        return ResponseEntity.ok(messages);
    }

    /**
     * 사용자가 채팅방에서 나가며 자신의 메시지를 soft delete 처리합니다.
     *
     * @param chatRoomId 나갈 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return No Content
     */
    @Operation(summary = "채팅방 나가기", description = "사용자가 특정 채팅방에서 나가며 자신의 메시지만 soft delete 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "나가기 성공"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content)
    })
    @DeleteMapping("/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
        @Parameter(description = "나갈 채팅방 ID", example = "1")
        @PathVariable("chatRoomId") Long chatRoomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        chatMessageService.leaveChatRoom(chatRoomId, loginUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * 채팅방의 상세 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return 채팅방 상세 정보
     */
    @Operation(summary = "채팅방 상세 정보 조회", description = "채팅방 ID를 기준으로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ChatRoomDetailDto.class)))
    })
    @GetMapping("/{roomId}/detail")
    public ResponseEntity<ChatRoomDetailDto> getChatRoomDetail(
        @Parameter(description = "상세 정보를 조회할 채팅방 ID", example = "1")
        @PathVariable("roomId") Long roomId,
        @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        return ResponseEntity.ok(chatRoomService.getChatRoomDetail(roomId, loginUser));
    }

    /**
     * 채팅방의 메시지를 읽음 처리합니다.
     *
     * @param chatRoomId 메시지를 읽음 처리할 채팅방 ID
     * @param loginUser 현재 로그인한 사용자
     * @return No Content
     */
    @Operation(summary = "메시지 읽음 처리", description = "현재 로그인한 사용자가 해당 채팅방의 메시지를 읽음 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공")
    })
    @PatchMapping("/{chatRoomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @Parameter(description = "읽음 처리할 채팅방 ID", example = "1")
            @PathVariable("chatRoomId") Long chatRoomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser
    ) {
        chatRoomService.markMessagesAsRead(chatRoomId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }
}
