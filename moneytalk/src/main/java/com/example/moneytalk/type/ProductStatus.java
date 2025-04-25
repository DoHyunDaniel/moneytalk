package com.example.moneytalk.type;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 중고 상품의 판매 상태를 나타내는 열거형(Enum)입니다.
 * - SALE: 판매 중
 * - RESERVED: 예약됨
 * - SOLD: 판매 완료
 */
@Schema(description = "상품 판매 상태")
public enum ProductStatus {

    @Schema(description = "판매 중 (구매 가능)")
    SALE,

    @Schema(description = "예약됨 (다른 사용자가 구매 예약)")
    RESERVED,

    @Schema(description = "판매 완료 (더 이상 구매 불가)")
    SOLD
}
