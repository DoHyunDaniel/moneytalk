package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

/**
 * 상품 등록 요청 시 사용되는 DTO입니다.
 * 제목, 설명, 가격, 카테고리, 위치, 상태 등의 정보를 포함합니다.
 */
@Getter
@Setter
@Schema(description = "상품 등록 요청 DTO")
public class ProductRequestDto {

    @Schema(description = "상품 제목", example = "아이폰 14 Pro 미개봉", required = true)
    @NotBlank(message = "상품 제목은 필수 입력 항목입니다.")
    private String title;

    @Schema(description = "상품 설명", example = "개봉만 했고 사용하지 않았습니다. 박스 구성품 모두 포함되어 있어요.", required = true)
    @NotBlank(message = "상품 설명은 필수 입력 항목입니다.")
    private String description;

    @Schema(description = "상품 가격 (원)", example = "1350000", required = true, minimum = "0")
    @NotNull(message = "가격은 필수 입력 항목입니다.")
    private Integer price;

    @Schema(description = "상품 카테고리", example = "전자기기", required = true)
    @NotBlank(message = "카테고리는 필수 입력 항목입니다.")
    private String category;

    @Schema(description = "상품 거래 위치", example = "서울특별시 강남구 역삼동", required = true)
    @NotBlank(message = "거래 위치는 필수 입력 항목입니다.")
    private String location;

    @Schema(description = "상품 상태 (SALE, RESERVED, SOLD 중 하나)", example = "SALE", defaultValue = "SALE")
    private ProductStatus status = ProductStatus.SALE;
}
