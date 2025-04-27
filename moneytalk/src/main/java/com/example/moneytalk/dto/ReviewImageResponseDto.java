package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 리뷰 이미지 응답 DTO입니다.
 * 리뷰에 첨부된 이미지의 ID와 URL을 제공합니다.
 */
@Getter
@Builder
@Schema(description = "리뷰 이미지 응답 DTO")
public class ReviewImageResponseDto {

    @Schema(description = "리뷰 이미지 ID", example = "501")
    private final Long id;

    @Schema(
        description = "리뷰 이미지 S3 URL",
        example = "https://moneytalk-s3.s3.ap-northeast-2.amazonaws.com/reviews/501.jpg"
    )
    private final String imageUrl;
}
