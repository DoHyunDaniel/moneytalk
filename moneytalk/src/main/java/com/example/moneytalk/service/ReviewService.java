package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.AverageRatingResponseDto;
import com.example.moneytalk.dto.ReviewRequestDto;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.dto.ReviewStatsDto;
import com.example.moneytalk.dto.ReviewUpdateRequestDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.ReviewRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ErrorCode;
import com.example.moneytalk.type.ProductStatus;

import lombok.RequiredArgsConstructor;

/**
 * ReviewService 사용자 간 거래 후 작성되는 리뷰(후기)를 처리하는 서비스입니다.
 *
 * [기능 설명] - 리뷰 작성 (이미지 포함) - 리뷰 단건/목록 조회 - 리뷰 수정 및 삭제 - 받은 리뷰 목록 조회 - 상품별 평균 평점
 * 및 리뷰 수 조회
 *
 * [검증 포인트] - 거래 완료된 상품에만 리뷰 작성 가능 - 구매자만 리뷰 작성 가능 - 리뷰는 한 번만 작성 가능 - 본인 리뷰만
 * 수정/삭제 가능
 *
 * [관련 서비스] - {@link ReviewImageService}: 리뷰 이미지 등록/삭제 처리
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final ReviewImageService reviewImageService;

	/**
	 * 리뷰를 작성하고, 첨부 이미지가 있다면 S3에 업로드합니다.
	 *
	 * @param dto        리뷰 작성 요청 DTO
	 * @param imageFiles 이미지 파일 리스트
	 * @param reviewer   리뷰 작성자
	 * @throws GlobalException 상품이 존재하지 않을 경우 {@link ErrorCode#PRODUCT_NOT_FOUND}
	 * @throws GlobalException 리뷰 작성이 금지되었을 때
	 *                         {@link ErrorCode#REVIEW_WRITE_FORBIDDEN}
	 * @throws GlobalException 상품이 아직 판매되지 않았을 때
	 *                         {@link ErrorCode#PRODUCT_NOT_SOLD_YET}
	 * @throws GlobalException 자기 자신에게 리뷰를 작성했을 때
	 *                         {@link ErrorCode#CANNOT_REVIEW_SELF}
	 * @throws GlobalException 리뷰가 이미 작성되어 있을 때
	 *                         {@link ErrorCode#REVIEW_ALREADY_WRITTEN}
	 * @throws GlobalException 리뷰 작성 대상을 찾을 수 없을 때 {@link ErrorCode#USER_NOT_FOUND}
	 * 
	 */
	@Transactional
	public void writeReviewWithImages(ReviewRequestDto dto, List<MultipartFile> imageFiles, User reviewer) {
		Product product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new GlobalException(ErrorCode.PRODUCT_NOT_FOUND));
		if (product.getBuyer() == null) {
			throw new GlobalException(ErrorCode.PRODUCT_NOT_SOLD_YET);
		}

		if (!product.getBuyer().getId().equals(reviewer.getId())) {
			throw new GlobalException(ErrorCode.REVIEW_WRITE_FORBIDDEN);
		}

		if (product.getStatus() != ProductStatus.SOLD) {
			throw new GlobalException(ErrorCode.PRODUCT_NOT_SOLD_YET);
		}

		if (reviewer.getId().equals(dto.getRevieweeId())) {
			throw new GlobalException(ErrorCode.CANNOT_REVIEW_SELF);
		}

		boolean exists = reviewRepository.existsByProductIdAndReviewerId(dto.getProductId(), reviewer.getId());
		if (exists) {
			throw new GlobalException(ErrorCode.REVIEW_ALREADY_WRITTEN);
		}

		User reviewee = userRepository.findById(dto.getRevieweeId())
				.orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

		Review review = Review.builder().product(product).reviewer(reviewer).reviewee(reviewee).rating(dto.getRating())
				.content(dto.getContent()).build();

		reviewRepository.save(review);

		// 리뷰 이미지 업로드 처리 추가
		if (imageFiles != null && !imageFiles.isEmpty()) {
			reviewImageService.uploadReviewImages(review.getId(), imageFiles);
		}
	}

	/**
	 * 특정 상품에 작성된 리뷰 목록을 이미지와 함께 조회합니다.
	 *
	 * @param productId 상품 ID
	 * @return 리뷰 응답 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsByProductId(Long productId) {
		List<Review> reviews = reviewRepository.findByProductId(productId);

		return reviews.stream().map(review -> {
			List<String> images = reviewImageService.getImageUrlsByReviewId(review.getId());
			return ReviewResponseDto.from(review, images);
		}).toList();
	}

	/**
	 * 리뷰 ID로 단건 리뷰 정보를 조회합니다.
	 *
	 * @param reviewId 리뷰 ID
	 * @return 리뷰 응답 DTO
	 * @throws GlobalException 리뷰가 존재하지 않을 때 {@link ErrorCode#REVIEW_NOT_FOUND}
	 */
	@Transactional(readOnly = true)
	public ReviewResponseDto getReviewById(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ErrorCode.REVIEW_NOT_FOUND));
		List<String> imageUrls = reviewImageService.getImageUrlsByReviewId(reviewId);
		return ReviewResponseDto.from(review, imageUrls);
	}

	/**
	 * 본인의 리뷰를 수정하고, 이미지도 새로 교체합니다.
	 *
	 * @param reviewId   리뷰 ID
	 * @param dto        수정 요청 DTO
	 * @param reviewer   수정 요청자
	 * @param imageFiles 새 이미지 리스트
	 * @throws GlobalException 리뷰가 존재하지 않을 때 {@link ErrorCode#REVIEW_NOT_FOUND}
	 * @throws GlobalException 리뷰를 업데이트할 권한이 없을 때 {@link ErrorCode#REVIEW_UPDATE_FORBIDDEN}
	 */
	@Transactional
	public void updateReviewWithImages(Long reviewId, ReviewUpdateRequestDto dto, User reviewer,
			List<MultipartFile> imageFiles) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new GlobalException(ErrorCode.REVIEW_UPDATE_FORBIDDEN);
		}

		review.setRating(dto.getRating());
		review.setContent(dto.getContent());

		// 기존 이미지 삭제 + 새 이미지 등록
		reviewImageService.deleteImagesByReviewId(reviewId);
		if (imageFiles != null && !imageFiles.isEmpty()) {
			reviewImageService.uploadReviewImages(reviewId, imageFiles);
		}
	}

	/**
	 * 본인의 리뷰를 삭제하고, 이미지도 함께 삭제합니다.
	 *
	 * @param reviewId 리뷰 ID
	 * @param reviewer 삭제 요청자
	 * @throws GlobalException 리뷰가 존재하지 않을 때 {@link ErrorCode#REVIEW_NOT_FOUND}
	 * @throws GlobalException 리뷰 삭제 권한이 없을 때 {@link ErrorCode#REVIEW_DELETE_FORBIDDEN}
	 */
	@Transactional
	public void deleteReview(Long reviewId, User reviewer) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new GlobalException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getReviewer().getId().equals(reviewer.getId())) {
			throw new GlobalException(ErrorCode.REVIEW_DELETE_FORBIDDEN);
		}
		reviewImageService.deleteImagesByReviewId(reviewId);
		reviewRepository.delete(review);
	}

	/**
	 * 특정 사용자가 받은 리뷰 목록을 조회합니다.
	 *
	 * @param user 리뷰 대상 사용자
	 * @return 받은 리뷰 리스트
	 */
	@Transactional(readOnly = true)
	public List<ReviewResponseDto> getReviewsReceivedByUser(User user) {
		List<Review> reviews = reviewRepository.findByReviewee(user);

		return reviews.stream().map(review -> {
			List<String> imageUrls = reviewImageService.getImageUrlsByReviewId(review.getId());
			return ReviewResponseDto.from(review, imageUrls);
		}).toList();
	}

	/**
	 * 상품에 대한 평균 평점과 리뷰 개수를 조회합니다.
	 *
	 * @param productId 상품 ID
	 * @return 평점 평균 및 개수를 담은 DTO
	 */
	@Transactional(readOnly = true)
	public AverageRatingResponseDto getAverageRatingInfo(Long productId) {
		ReviewStatsDto stats = reviewRepository.findReviewStatsByProductId(productId);

		Long count = 0L;
		Double avg = 0.0;

		return AverageRatingResponseDto.builder().productId(productId).averageRating(stats.getAverageRating())
				.reviewCount(stats.getReviewCount()).build();
	}

}
