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

/**
 * ReviewImageService
 * 리뷰 이미지의 업로드, 조회, 삭제 기능을 처리하는 서비스입니다.
 *
 * [기능 설명]
 * - 리뷰에 첨부된 이미지들을 AWS S3에 업로드하거나 삭제합니다.
 * - 리뷰 ID를 기준으로 이미지 URL을 조회할 수 있습니다.
 *
 * [관련 요소]
 * - {@link com.example.moneytalk.config.S3Uploader}: S3 파일 업로드/삭제 도우미 클래스
 * - {@link com.example.moneytalk.domain.ReviewImage}: 리뷰 이미지 엔티티
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final S3Uploader s3Uploader;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    /**
     * 리뷰에 첨부된 이미지 파일들을 업로드하고, 해당 URL을 반환합니다.
     *
     * @param reviewId 연결할 리뷰 ID
     * @param imageFiles 업로드할 이미지 파일 리스트
     * @return 업로드된 이미지의 S3 URL 리스트
     * @throws IllegalArgumentException 리뷰가 존재하지 않을 경우
     */
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

    /**
     * 특정 리뷰에 연결된 이미지 URL 목록을 조회합니다.
     *
     * @param reviewId 리뷰 ID
     * @return 이미지 URL 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getImageUrlsByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
        return images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();
    }

    /**
     * 특정 리뷰에 연결된 이미지들을 S3에서 삭제하고, DB에서도 제거합니다.
     *
     * @param reviewId 리뷰 ID
     */
    @Transactional
    public void deleteImagesByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
        images.forEach(image -> s3Uploader.deleteFile(image.getImageUrl())); // S3에서도 삭제
        reviewImageRepository.deleteAll(images);
    }
}
