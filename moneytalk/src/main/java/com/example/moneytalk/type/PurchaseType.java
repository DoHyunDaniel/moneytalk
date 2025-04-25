package com.example.moneytalk.type;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 거래의 유형을 나타내는 열거형(Enum)입니다.
 * - PURCHASE: 내가 구매한 거래
 * - SALE: 내가 판매한 거래
 */
@Schema(description = "거래 유형 (구매 또는 판매)")
public enum PurchaseType {

    @Schema(description = "구매한 거래")
    PURCHASE,

    @Schema(description = "판매한 거래")
    SALE
}
