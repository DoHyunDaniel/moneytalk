package com.example.moneytalk.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.AverageRatingResponseDto;
import com.example.moneytalk.dto.ReviewRequestDto;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.dto.ReviewUpdateRequestDto;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.ReviewRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final ReviewImageService reviewImageService;

	@Transactional
	public void writeReviewWithImages(ReviewRequestDto dto, List<MultipartFile> imageFiles, User reviewer) {
		Product product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
		if (product.getBuyer() == null) {
			throw new IllegalStateException("아직 구매가 확정되지 않은 상품입니다.");
		}

		if (!product.getBuyer().getId().equals(reviewer.getId())) {
			throw new AccessDeniedException("해당 상품을 구매한 사용자만 리뷰를 작성할 수 있습니다.");
		}

		if (product.getStatus() != ProductStatus.SOLD) {
			throw new IllegalStateException("해당 상품은 아직 거래 완료되지 않았습니다.");
		}

		if (reviewer.getId().equals(dto.getTargetUserId())) {
			throw new IllegalArgumentException("자기 자신에게는 리뷰를 작성할 수 없습니다.");
		}

		boolean exists = reviewRepository.existsByProductIdAndReviewerId(dto.getProductId(), reviewer.getId());
		if (exists) {
			throw new IllegalStateException("이미 리뷰를 작성한 상품입니다.");
		}

		User target = userRepository.findById(dto.getTargetUserId())
				.orElseThrow(() -> new IllegalArgumentException("대상 유저가 존재하지 않습니다."));

		Review review = Review.builder().product(product).reviewer(reviewer).target(target).rating(dto.getRating())
				.content(dto.getContent()).build();

		reviewRepository.save(review);

		// 리뷰 이미지 업로드 처리 추가
		if (imageFiles != null && !imageFiles.isEmpty()) {
			reviewImageService.uploadReviewImages(review.getId(), imageFiles);
		}
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsByProductId(Long productId) {
	    List<Review> reviews = reviewRepository.findByProductId(productId);

	    return reviews.stream()
	            .map(review -> {
	                List<String> images = reviewImageService.getImageUrlsByReviewId(review.getId());
	                return ReviewResponseDto.from(review, images);
	            })
	            .toList();
	}

	@Transactional(readOnly = true)
	public ReviewResponseDto getReviewById(Long reviewId) {
	    Review review = reviewRepository.findById(reviewId)
	            .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
	    List<String> imageUrls = reviewImageService.getImageUrlsByReviewId(reviewId);
	    return ReviewResponseDto.from(review, imageUrls);
	}

	@Transactional
	public void updateReviewWithImages(Long reviewId, ReviewUpdateRequestDto dto, User reviewer, List<MultipartFile> imageFiles) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new IllegalStateException("본인의 리뷰만 수정할 수 있습니다.");
		}

		review.setRating(dto.getRating());
		review.setContent(dto.getContent());

		// 기존 이미지 삭제 + 새 이미지 등록
		reviewImageService.deleteImagesByReviewId(reviewId);
		if (imageFiles != null && !imageFiles.isEmpty()) {
			reviewImageService.uploadReviewImages(reviewId, imageFiles);
		}
	}

	@Transactional
	public void deleteReview(Long reviewId, User reviewer) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
		}
		reviewImageService.deleteImagesByReviewId(reviewId);
		reviewRepository.delete(review);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsReceivedByUser(User user) {
	    List<Review> reviews = reviewRepository.findByTarget(user);

	    return reviews.stream()
	            .map(review -> {
	                List<String> imageUrls = reviewImageService.getImageUrlsByReviewId(review.getId());
	                return ReviewResponseDto.from(review, imageUrls);
	            })
	            .toList();
	}


	@Transactional(readOnly = true)
	public AverageRatingResponseDto getAverageRatingInfo(Long productId) {
		List<Object[]> resultList = reviewRepository.findReviewCountAndAverageRatingByProductId(productId);

		Long count = 0L;
		Double avg = 0.0;

		if (!resultList.isEmpty()) {
			Object[] result = resultList.get(0);
			count = result[0] != null ? ((Number) result[0]).longValue() : 0L;
			avg = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
		}

		return AverageRatingResponseDto.builder().productId(productId).averageRating(avg).reviewCount(count).build();
	}
	


}
