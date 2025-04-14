package com.example.moneytalk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.ReviewImage;
import com.example.moneytalk.repository.ReviewImageRepository;
import com.example.moneytalk.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final S3Uploader s3Uploader;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Transactional
    public List<String> uploadReviewImages(Long reviewId, List<MultipartFile> imageFiles) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        List<String> uploadedUrls = imageFiles.stream()
                .map(file -> {
                    String imageUrl = s3Uploader.uploadFile(file, "review-images");
                    ReviewImage reviewImage = ReviewImage.builder()
                            .review(review)
                            .imageUrl(imageUrl)
                            .build();
                    reviewImageRepository.save(reviewImage);
                    return imageUrl;
                })
                .collect(Collectors.toList());

        return uploadedUrls;
    }
    
    @Transactional(readOnly = true)
    public List<String> getImageUrlsByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
        return images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();
    }

    @Transactional
    public void deleteImagesByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
        images.forEach(image -> s3Uploader.deleteFile(image.getImageUrl())); // S3에서도 삭제
        reviewImageRepository.deleteAll(images);
    }


}
