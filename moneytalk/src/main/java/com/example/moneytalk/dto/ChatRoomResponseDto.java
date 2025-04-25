package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 생성 또는 조회 후 응답으로 사용되는 DTO입니다.
 * 채팅방 ID, 상품 ID, 참여자 닉네임, 마지막 메시지 정보 등을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "채팅방 생성 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1001", required = true)
    private Long chatRoomId;

    @Schema(description = "채팅이 연결된 상품 ID", example = "2002", required = true)
    private Long productId;

    @Schema(description = "구매자 닉네임", example = "dohyunnn", required = true)
    private String buyerNickname;

    @Schema(description = "판매자 닉네임", example = "johnny92", required = true)
    private String sellerNickname;

    @Schema(description = "채팅방 마지막 메시지 내용", example = "안녕하세요. 구매 원합니다.")
    private String lastMessage;

    @Schema(description = "채팅방 마지막 메시지 전송 시간", example = "2025-04-25T15:45:00")
    private LocalDateTime lastMessageAt;

    /**
     * ChatRoom 엔티티로부터 ChatRoomResponseDto를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param room ChatRoom 엔티티
     * @return ChatRoomResponseDto 인스턴스
     */
    public static ChatRoomResponseDto from(ChatRoom room) {
        return ChatRoomResponseDto.builder()
            .chatRoomId(room.getId())
            .productId(room.getProduct().getId())
            .buyerNickname(room.getBuyer().getNickname())
            .sellerNickname(room.getSeller().getNickname())
            .lastMessage(room.getLastMessage())
            .lastMessageAt(room.getLastMessageAt())
            .build();
    }
}
