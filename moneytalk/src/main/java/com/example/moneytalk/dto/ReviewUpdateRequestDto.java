package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 리뷰 수정 요청 DTO입니다.
 * 평점과 리뷰 본문 내용을 수정할 수 있습니다.
 */
@Getter
@Setter
@Schema(description = "리뷰 수정 요청 DTO")
public class ReviewUpdateRequestDto {

    @Schema(
        description = "평점 (1~5점 사이 정수)",
        example = "4",
        minimum = "1",
        maximum = "5",
        required = true
    )
    @Min(value = 1, message = "최소 평점은 1점입니다.")
    @Max(value = 5, message = "최대 평점은 5점입니다.")
    private int rating;

    @Schema(
        description = "수정할 리뷰 내용",
        example = "상품 상태가 좋았어요. 거래도 빠르게 진행되었습니다.",
        required = true
    )
    @NotBlank(message = "리뷰 내용은 공백일 수 없습니다.")
    private String content;
}
