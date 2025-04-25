package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 상품 상태 변경 요청 DTO입니다.
 * 상품을 판매중, 예약중, 판매 완료 상태로 변경할 때 사용됩니다.
 */
@Getter
@Schema(description = "상품 상태 변경 요청 DTO")
public class ProductStatusUpdateRequestDto {

    @NotNull(message = "상품 상태를 입력해주세요.")
    @Schema(
        description = "변경할 상품 상태 (SALE: 판매중, RESERVED: 예약중, SOLD: 판매완료)",
        example = "SOLD",
        required = true
    )
    private ProductStatus status;
}
