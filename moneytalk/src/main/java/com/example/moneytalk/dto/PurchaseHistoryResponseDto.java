package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.PurchaseHistory;
import com.example.moneytalk.type.PurchaseType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseHistoryResponseDto {

    private Long id;
    private Long productId;
    private String title;
    private Integer price;
    private PurchaseType type;
    private LocalDateTime createdAt;

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
