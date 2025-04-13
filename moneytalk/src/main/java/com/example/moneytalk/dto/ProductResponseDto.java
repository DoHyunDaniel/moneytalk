package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import com.example.moneytalk.type.ProductStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private String category;
    private String location;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private String sellerNickname; // 유저 정보 일부도 포함
}
