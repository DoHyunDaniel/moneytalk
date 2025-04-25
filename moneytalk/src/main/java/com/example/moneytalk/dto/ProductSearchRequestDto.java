package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 상품 검색 필터 및 정렬 요청 DTO입니다.
 * 키워드, 카테고리, 위치, 가격 범위, 상품 상태, 정렬 조건 등을 지정할 수 있습니다.
 */
@Getter
@Setter
@Schema(description = "상품 검색 요청 파라미터 DTO")
public class ProductSearchRequestDto {

    @Schema(
        description = "검색 키워드 (제목 또는 설명에 포함될 단어)",
        example = "노트북"
    )
    private String keyword;

    @Schema(
        description = "카테고리 필터 (예: 전자기기, 의류 등)",
        example = "전자기기"
    )
    private String category;

    @Schema(
        description = "지역 필터 (예: 서울, 부산 등)",
        example = "서울"
    )
    private String location;

    @Schema(
        description = "최소 가격 필터",
        example = "10000",
        minimum = "0"
    )
    private Integer minPrice;

    @Schema(
        description = "최대 가격 필터",
        example = "1000000",
        minimum = "0"
    )
    private Integer maxPrice;

    @Schema(
        description = "상품 상태 필터 (SALE, RESERVED, SOLD)",
        example = "SALE"
    )
    private ProductStatus status;

    @Schema(
        description = "정렬 기준 (createdAt, price_asc, price_desc)",
        example = "price_desc"
    )
    private String sort;
}
