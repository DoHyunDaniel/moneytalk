package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 리뷰 통계 응답 DTO입니다.
 * 리뷰 개수 및 평균 평점을 포함합니다.
 */
@Getter
@AllArgsConstructor
@Schema(description = "리뷰 통계 DTO (리뷰 개수 및 평균 평점)")
public class ReviewStatsDto {

    @Schema(description = "총 리뷰 개수", example = "12")
    private Long reviewCount;

    @Schema(description = "평균 평점 (0.0 ~ 5.0)", example = "4.3")
    private Double averageRating;
}
