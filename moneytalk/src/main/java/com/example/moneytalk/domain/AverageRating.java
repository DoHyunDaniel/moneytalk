package com.example.moneytalk.domain;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class AverageRating {

    private Long productId;
    private Double averageRating;
    private Long reviewCount;

    @QueryProjection
    public AverageRating(Long productId, Double averageRating, Long reviewCount) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }
}
