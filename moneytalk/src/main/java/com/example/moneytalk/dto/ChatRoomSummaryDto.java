package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 목록 조회 시 사용되는 요약 정보 DTO입니다.
 * 상품/상대방/메시지 관련 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "채팅방 목록 요약 DTO")
public class ChatRoomSummaryDto {

    @Schema(description = "채팅방 ID", example = "1001")
    private final Long chatRoomId;

    @Schema(description = "상품 ID", example = "2002")
    private final Long productId;

    @Schema(description = "상품 제목", example = "갤럭시 Z 플립 5 미개봉")
    private final String productTitle;

    @Schema(description = "상품 썸네일 이미지 URL", example = "https://moneytalk.s3.ap-northeast-2.amazonaws.com/products/thumbnail.jpg")
    private final String productThumbnailUrl;

    @Schema(description = "채팅 상대방 닉네임", example = "jenny88")
    private final String opponentNickname;

    @Schema(description = "채팅 상대방 프로필 이미지 URL", example = "https://moneytalk.s3.ap-northeast-2.amazonaws.com/profiles/jenny88.jpg")
    private final String opponentProfileImage;

    @Schema(description = "마지막 메시지 내용", example = "가격 조정 가능할까요?")
    private final String lastMessage;

    @Schema(description = "마지막 메시지 전송 시간", example = "2025-04-25T16:30:00")
    private final LocalDateTime lastMessageAt;

    @Schema(description = "채팅방이 종료되었는지 여부", example = "false")
    private boolean isClosed;

    @Schema(description = "채팅 상대방의 사용자 ID", example = "15")
    private final Long opponentUserId;

    @Schema(description = "안 읽은 메시지 수", example = "2")
    private final Integer unreadCount;
}
