package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 리뷰 작성 요청 DTO입니다.
 * 대상 상품, 리뷰 대상 사용자, 평점, 내용 등 필수 정보들을 포함합니다.
 */
@Getter
@Setter
@Schema(description = "리뷰 작성 요청 DTO")
public class ReviewRequestDto {

    @Schema(description = "리뷰 대상 상품 ID", example = "1", required = true)
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @Schema(description = "리뷰 대상 사용자 ID (판매자 또는 구매자)", example = "2", required = true)
    @NotNull(message = "리뷰 대상 사용자 ID는 필수입니다.")
    private Long revieweeId;

    @Schema(description = "평점 (1~5)", example = "5", minimum = "1", maximum = "5", required = true)
    @Min(value = 1, message = "최소 평점은 1점입니다.")
    @Max(value = 5, message = "최대 평점은 5점입니다.")
    private int rating;

    @Schema(description = "리뷰 본문 내용", example = "정말 좋은 거래였습니다!", required = true)
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;
}
