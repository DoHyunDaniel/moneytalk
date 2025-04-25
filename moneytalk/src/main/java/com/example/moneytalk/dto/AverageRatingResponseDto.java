package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 특정 상품에 대한 평균 평점 및 리뷰 개수를 응답하는 DTO입니다.
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "상품 평균 평점 응답 DTO")
public class AverageRatingResponseDto {

    @Schema(description = "평점을 계산할 상품의 고유 ID", example = "42", required = true)
    private Long productId;

    @Schema(description = "해당 상품의 평균 평점 (0.0 ~ 5.0)", example = "4.5", minimum = "0", maximum = "5")
    private Double averageRating;

    @Schema(description = "총 리뷰 개수", example = "10", minimum = "0")
    private Long reviewCount;
}
