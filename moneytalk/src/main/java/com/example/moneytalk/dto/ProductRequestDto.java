package com.example.moneytalk.dto;

import com.example.moneytalk.type.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "상품 등록 요청 DTO")
public class ProductRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Integer price;

    @NotBlank
    private String category;

    @NotBlank
    private String location;

    @Schema(description = "상품 상태", example = "SALE")
    private ProductStatus status = ProductStatus.SALE;
}
