package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public void writeReview(ReviewRequestDto dto, User reviewer) {
		Product product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

		if (product.getStatus() != ProductStatus.SOLD) {
			throw new IllegalStateException("해당 상품은 아직 거래 완료되지 않았습니다.");
		}

		if (reviewer.getId().equals(dto.getTargetUserId())) {
			throw new IllegalArgumentException("자기 자신에게는 리뷰를 작성할 수 없습니다.");
		}

		boolean exists = reviewRepository.existsByProductIdAndReviewerId(dto.getProductId(), reviewer.getId());
		if (exists) {
			throw new IllegalStateException("해당 상품에 대한 리뷰는 이미 작성되었습니다.");
		}

		User target = userRepository.findById(dto.getTargetUserId())
				.orElseThrow(() -> new IllegalArgumentException("대상 유저가 존재하지 않습니다."));

		Review review = Review.builder().product(product).reviewer(reviewer).target(target).rating(dto.getRating())
				.content(dto.getContent()).build();

		reviewRepository.save(review);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsByProductId(Long productId) {
		List<Review> reviews = reviewRepository.findByProductId(productId);

		return reviews.stream()
				.map(ReviewResponseDto::from)
				.toList();
	}

	@Transactional
	public void updateReview(Long reviewId, ReviewUpdateRequestDto dto, User reviewer) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new IllegalStateException("본인의 리뷰만 수정할 수 있습니다.");
		}

		review.setRating(dto.getRating());
		review.setContent(dto.getContent());
	}

	@Transactional
	public void deleteReview(Long reviewId, User reviewer) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
		}

		reviewRepository.delete(review);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsReceivedByUser(User user) {
		List<Review> reviews = reviewRepository.findByTarget(user);
		return reviews.stream().map(ReviewResponseDto::from).toList();
	}


	@Transactional(readOnly = true)
	public AverageRatingResponseDto getAverageRatingInfo(Long productId) {
	    Double avg = reviewRepository.findAverageRatingByProductId(productId);
	    long count = reviewRepository.countByProductId(productId);

	    return AverageRatingResponseDto.builder()
	            .productId(productId)
	            .averageRating(avg != null ? avg : 0.0)
	            .reviewCount(count)
	            .build();
	}

}
