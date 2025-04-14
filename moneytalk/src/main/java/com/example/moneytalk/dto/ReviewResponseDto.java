package com.example.moneytalk.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.moneytalk.domain.Review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponseDto {

    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "상품 ID", example = "5")
    private Long productId;
    
    @Schema(description = "리뷰 작성자 ID", example = "3")
    private Long reviewerId;
    
    @Schema(description = "리뷰 대상자 ID", example = "1")
    private Long targetId;
    
    @Schema(description = "리뷰 작성자 닉네임", example = "david123")
    private String reviewerNickname;

    @Schema(description = "리뷰 평점 (1~5)", example = "5")
    private int rating;

    @Schema(description = "리뷰 내용", example = "친절하고 시간도 잘 지켜주셨어요!")
    private String content;

    @Schema(description = "리뷰 이미지 URL 목록")
    private List<String> imageUrls;
    
    @Schema(description = "리뷰 작성 일시", example = "2025-04-11T14:35:00")
    private LocalDateTime createdAt;

    public static ReviewResponseDto from(Review review, List<String> imageUrls) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .productId(review.getProduct().getId())
                .reviewerId(review.getReviewer().getId())
                .targetId(review.getTarget().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }

}
