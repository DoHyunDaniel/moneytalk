package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateRequestDto {

    @Schema(description = "평점 (1~5)", example = "4")
    @Min(value = 1)
    @Max(value = 5)
    private int rating;

    @Schema(description = "리뷰 내용", example = "상품 상태가 좋았어요.")
    @NotBlank
    private String content;
}

