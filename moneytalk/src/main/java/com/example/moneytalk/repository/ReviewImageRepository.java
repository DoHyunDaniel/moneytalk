package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewId(Long reviewId);
}
