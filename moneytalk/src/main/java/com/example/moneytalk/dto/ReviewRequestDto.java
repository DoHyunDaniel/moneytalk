package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

    @Schema(description = "리뷰 대상 상품 ID", example = "1")
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @Schema(description = "리뷰 대상 사용자 ID", example = "2")
    @NotNull(message = "리뷰 대상 사용자 ID는 필수입니다.")
    private Long targetUserId;

    @Schema(description = "평점 (1~5)", example = "5", minimum = "1", maximum = "5")
    @Min(value = 1, message = "최소 평점은 1점입니다.")
    @Max(value = 5, message = "최대 평점은 5점입니다.")
    private int rating;

    @Schema(description = "리뷰 내용", example = "정말 좋은 거래였습니다!")
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;
}
