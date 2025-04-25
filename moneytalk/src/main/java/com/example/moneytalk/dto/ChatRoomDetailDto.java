package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅방 상세 정보 응답 DTO입니다.
 * 채팅방 ID, 상품 정보, 상대방 사용자 정보를 포함합니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "채팅방 상세 정보 응답 DTO")
public class ChatRoomDetailDto {

    @Schema(description = "채팅방 ID", example = "1001", required = true)
    private Long chatRoomId;

    @Schema(description = "채팅 대상 상품 ID", example = "2002", required = true)
    private Long productId;

    @Schema(description = "상품 제목", example = "아이폰 14 Pro 미개봉", required = true)
    private String productTitle;

    @Schema(description = "채팅 상대방 닉네임", example = "johnny92", required = true)
    private String opponentNickname;

    @Schema(description = "채팅 상대방 프로필 이미지 URL", example = "https://moneytalk.s3.ap-northeast-2.amazonaws.com/profile/johnny92.jpg")
    private String opponentProfileImage;
}
