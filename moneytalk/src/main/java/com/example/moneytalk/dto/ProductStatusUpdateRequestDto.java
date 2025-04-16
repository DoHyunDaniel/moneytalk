package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductStatusUpdateRequestDto {
    @NotNull(message = "상품 상태를 입력해주세요.")
    private ProductStatus status;
}
