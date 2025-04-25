package com.example.moneytalk.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.moneytalk.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 리뷰 상세 조회 또는 목록 응답 DTO입니다.
 * 작성자/대상자 ID, 상품, 평점, 내용, 이미지 URL 등을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponseDto {

    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "리뷰 대상 상품 ID", example = "5")
    private Long productId;
    
    @Schema(description = "리뷰 작성자 ID", example = "3")
    private Long reviewerId;
    
    @Schema(description = "리뷰 대상자 ID", example = "1")
    private Long revieweeId;
    
    @Schema(description = "리뷰 작성자 닉네임", example = "david123")
    private String reviewerNickname;

    @Schema(description = "평점 (1~5)", example = "5")
    private int rating;

    @Schema(description = "리뷰 내용", example = "친절하고 시간도 잘 지켜주셨어요!")
    private String content;

    @Schema(description = "리뷰에 첨부된 이미지 URL 목록", example = "[\"https://.../review1.jpg\", \"https://.../review2.jpg\"]")
    private List<String> imageUrls;
    
    @Schema(description = "리뷰 작성 일시", example = "2025-04-11T14:35:00")
    private LocalDateTime createdAt;

    /**
     * Review 엔티티와 이미지 URL 목록으로부터 ReviewResponseDto를 생성합니다.
     *
     * @param review 리뷰 엔티티
     * @param imageUrls 이미지 URL 리스트
     * @return 응답 DTO 객체
     */
    public static ReviewResponseDto from(Review review, List<String> imageUrls) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .productId(review.getProduct().getId())
                .reviewerId(review.getReviewer().getId())
                .revieweeId(review.getReviewee().getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
