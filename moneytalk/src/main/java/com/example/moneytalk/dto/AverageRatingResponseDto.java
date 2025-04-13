package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "상품 평균 평점 응답 DTO")
public class AverageRatingResponseDto {

    @Schema(description = "상품 ID", example = "42")
    private Long productId;

    @Schema(description = "리뷰 평균 평점", example = "4.5")
    private double averageRating;

    @Schema(description = "리뷰 개수", example = "10")
    private long reviewCount;
}
