package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.PurchaseHistory;
import com.example.moneytalk.type.PurchaseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 구매/판매 이력 조회 응답 DTO입니다.
 * 상품명, 가격, 거래 유형, 거래 시각 정보를 포함합니다.
 */
@Getter
@Builder
@Schema(description = "구매/판매 이력 응답 DTO")
public class PurchaseHistoryResponseDto {

    @Schema(description = "이력 ID", example = "3001")
    private final Long id;

    @Schema(description = "상품 ID", example = "101")
    private final Long productId;

    @Schema(description = "상품 제목", example = "아이패드 미니 6세대")
    private final String title;

    @Schema(description = "거래 가격 (원)", example = "550000")
    private final Integer price;

    @Schema(description = "거래 유형 (PURCHASE: 구매, SALE: 판매)", example = "PURCHASE")
    private final PurchaseType type;

    @Schema(description = "거래 일시", example = "2025-04-25T17:00:00")
    private final LocalDateTime createdAt;

    /**
     * PurchaseHistory 엔티티로부터 DTO를 생성하는 팩토리 메서드입니다.
     *
     * @param history PurchaseHistory 엔티티
     * @return DTO 객체
     */
    public static PurchaseHistoryResponseDto from(PurchaseHistory history) {
        Product product = history.getProduct();
        return PurchaseHistoryResponseDto.builder()
                .id(history.getId())
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .type(history.getType())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
