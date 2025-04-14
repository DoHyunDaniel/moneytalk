package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "상품 검색 요청 파라미터")
public class ProductSearchRequest {

    @Schema(description = "검색 키워드 (제목 또는 설명 포함)", example = "노트북")
    private String keyword;

    @Schema(description = "카테고리 필터", example = "전자기기")
    private String category;

    @Schema(description = "지역 필터", example = "서울")
    private String location;

    @Schema(description = "최소 가격", example = "10000")
    private Integer minPrice;

    @Schema(description = "최대 가격", example = "1000000")
    private Integer maxPrice;

    @Schema(description = "상품 상태 (SALE, RESERVED, SOLD)", example = "SALE")
    private ProductStatus status;

    @Schema(description = "정렬 기준 (createdAt, price_asc, price_desc)", example = "price_desc")
    private String sort;
}
