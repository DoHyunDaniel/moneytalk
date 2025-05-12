package com.example.moneytalk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@InjectMocks
	private ReviewService reviewService;

	@Mock
	private ProductRepository productRepository;
	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ReviewImageService reviewImageService;

	@Test
	void 리뷰작성_성공_이미지_포함() {
		// given
		Long productId = 1L;
		Long revieweeId = 2L;
		Long reviewerId = 3L;

		User reviewer = User.builder().id(reviewerId).nickname("리뷰어").build();
		User reviewee = User.builder().id(revieweeId).nickname("판매자").build();
		Product product = Product.builder().id(productId).buyer(reviewer) // 구매자가 본인
				.status(ProductStatus.SOLD).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(revieweeId).rating(5)
				.content("좋은 거래였습니다.").build();

		List<MultipartFile> imageFiles = List
				.of(new MockMultipartFile("images", "img1.jpg", "image/jpeg", "test-image".getBytes()));

		given(productRepository.findById(productId)).willReturn(Optional.of(product));
		given(userRepository.findById(revieweeId)).willReturn(Optional.of(reviewee));
		given(reviewRepository.existsByProductIdAndReviewerId(productId, reviewerId)).willReturn(false);
		given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
			Review r = invocation.getArgument(0);
			r.setId(100L);
			return r;
		});

		// when
		reviewService.writeReviewWithImages(dto, imageFiles, reviewer);

		// then
		verify(reviewRepository).save(any(Review.class));
		verify(reviewImageService).uploadReviewImages(eq(100L), eq(imageFiles));
	}

	@Test
	void 리뷰작성_실패_구매자가_아님() {
		// given
		Long productId = 1L;
		Long revieweeId = 2L;
		Long reviewerId = 3L;

		User reviewer = User.builder().id(reviewerId).build(); // 리뷰어
		User buyer = User.builder().id(999L).build(); // 다른 사람
		Product product = Product.builder().id(productId).buyer(buyer).status(ProductStatus.SOLD).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(revieweeId).rating(5)
				.content("좋아요").build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.writeReviewWithImages(dto, List.of(), reviewer));
		assertEquals(ErrorCode.REVIEW_WRITE_FORBIDDEN, ex.getErrorCode());
	}

	@Test
	void 리뷰작성_실패_상품_판매되지_않음() {
		// given
		Long productId = 1L;
		Long revieweeId = 2L;
		Long reviewerId = 3L;

		User reviewer = User.builder().id(reviewerId).build();
		Product product = Product.builder().id(productId).buyer(null) // 아직 구매자 없음
				.status(ProductStatus.SALE).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(revieweeId).rating(4)
				.content("구매 전 리뷰").build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.writeReviewWithImages(dto, List.of(), reviewer));
		assertEquals(ErrorCode.PRODUCT_NOT_SOLD_YET, ex.getErrorCode());
	}

	@Test
	void 리뷰작성_실패_자기자신에게_리뷰() {
		// given
		Long productId = 1L;
		Long reviewerId = 3L;

		User reviewer = User.builder().id(reviewerId).build();
		Product product = Product.builder().id(productId).buyer(reviewer).status(ProductStatus.SOLD).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(reviewerId) // 자기 자신에게 리뷰
				.rating(3).content("자화자찬").build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.writeReviewWithImages(dto, List.of(), reviewer));
		assertEquals(ErrorCode.CANNOT_REVIEW_SELF, ex.getErrorCode());
	}

	@Test
	void 리뷰작성_실패_이미_작성됨() {
		// given
		Long productId = 1L;
		Long reviewerId = 3L;
		Long revieweeId = 2L;

		User reviewer = User.builder().id(reviewerId).build();
		User reviewee = User.builder().id(revieweeId).build();
		Product product = Product.builder().id(productId).buyer(reviewer).status(ProductStatus.SOLD).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(revieweeId).rating(5)
				.content("또 리뷰를?").build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));
		given(reviewRepository.existsByProductIdAndReviewerId(productId, reviewerId)).willReturn(true);

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.writeReviewWithImages(dto, List.of(), reviewer));
		assertEquals(ErrorCode.REVIEW_ALREADY_WRITTEN, ex.getErrorCode());
	}

	@Test
	void 리뷰작성_실패_리뷰대상_유저없음() {
		// given
		Long productId = 1L;
		Long reviewerId = 3L;
		Long revieweeId = 2L;

		User reviewer = User.builder().id(reviewerId).build();
		Product product = Product.builder().id(productId).buyer(reviewer).status(ProductStatus.SOLD).build();

		ReviewRequestDto dto = ReviewRequestDto.builder().productId(productId).revieweeId(revieweeId).rating(4)
				.content("유령에게 리뷰").build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));
		given(reviewRepository.existsByProductIdAndReviewerId(productId, reviewerId)).willReturn(false);
		given(userRepository.findById(revieweeId)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.writeReviewWithImages(dto, List.of(), reviewer));
		assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void 리뷰목록조회_성공_이미지포함() {
		// given
		Long productId = 1L;

		User reviewer = User.builder().id(10L).nickname("reviewer1").build();
		User reviewee = User.builder().id(20L).nickname("reviewee1").build();
		Product product = Product.builder().id(productId).build();

		Review review = Review.builder().id(100L).product(product).reviewer(reviewer).reviewee(reviewee).rating(5)
				.content("정말 좋아요!").build();

		List<String> imageUrls = List.of("https://s3.bucket/review-100-1.jpg");

		given(reviewRepository.findByProductId(productId)).willReturn(List.of(review));
		given(reviewImageService.getImageUrlsByReviewId(100L)).willReturn(imageUrls);

		// when
		List<ReviewResponseDto> result = reviewService.getReviewsByProductId(productId);

		// then
		assertEquals(1, result.size());

		ReviewResponseDto dto = result.get(0);
		assertEquals(100L, dto.getReviewId());
		assertEquals(productId, dto.getProductId());
		assertEquals("정말 좋아요!", dto.getContent());
		assertEquals(5, dto.getRating());
		assertEquals(imageUrls, dto.getImageUrls());
		assertEquals("reviewer1", dto.getReviewerNickname());

		verify(reviewRepository).findByProductId(productId);
		verify(reviewImageService).getImageUrlsByReviewId(100L);
	}

	@Test
	void 리뷰단건조회_성공_이미지포함() {
		// given
		Long reviewId = 100L;
		Long productId = 1L;

		User reviewer = User.builder().id(10L).nickname("reviewer1").build();
		User reviewee = User.builder().id(20L).nickname("reviewee1").build();
		Product product = Product.builder().id(productId).build();

		Review review = Review.builder().id(reviewId).product(product).reviewer(reviewer).reviewee(reviewee).rating(4)
				.content("좋은 리뷰입니다.").build();

		List<String> imageUrls = List.of("https://s3.bucket/review-100.jpg");

		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
		given(reviewImageService.getImageUrlsByReviewId(reviewId)).willReturn(imageUrls);

		// when
		ReviewResponseDto result = reviewService.getReviewById(reviewId);

		// then
		assertEquals(reviewId, result.getReviewId());
		assertEquals(4, result.getRating());
		assertEquals("좋은 리뷰입니다.", result.getContent());
		assertEquals(imageUrls, result.getImageUrls());

		verify(reviewRepository).findById(reviewId);
		verify(reviewImageService).getImageUrlsByReviewId(reviewId);
	}

	@Test
	void 리뷰단건조회_실패_리뷰없음() {
		// given
		Long reviewId = 999L;
		given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class, () -> reviewService.getReviewById(reviewId));
		assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getErrorCode());

		verify(reviewRepository).findById(reviewId);
	}

	@Test
	void 리뷰수정_성공_이미지교체() {
		// given
		Long reviewId = 100L;
		Long reviewerId = 10L;

		User reviewer = User.builder().id(reviewerId).build();
		Review review = Review.builder().id(reviewId).reviewer(reviewer).rating(3).content("이전 내용").build();

		ReviewUpdateRequestDto dto = new ReviewUpdateRequestDto();
		dto.setRating(5);
		dto.setContent("수정된 내용");

		List<MultipartFile> newImages = List
				.of(new MockMultipartFile("images", "new.jpg", "image/jpeg", "new-content".getBytes()));

		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

		// when
		reviewService.updateReviewWithImages(reviewId, dto, reviewer, newImages);

		// then
		assertEquals(5, review.getRating());
		assertEquals("수정된 내용", review.getContent());
		verify(reviewImageService).deleteImagesByReviewId(reviewId);
		verify(reviewImageService).uploadReviewImages(reviewId, newImages);
	}

	@Test
	void 리뷰수정_실패_리뷰없음() {
		// given
		Long reviewId = 999L;
		ReviewUpdateRequestDto dto = new ReviewUpdateRequestDto();
		dto.setRating(4);
		dto.setContent("리뷰 없음");

		given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.updateReviewWithImages(reviewId, dto, User.builder().id(1L).build(), List.of()));
		assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void 리뷰수정_실패_작성자아님() {
		// given
		Long reviewId = 100L;
		User reviewer = User.builder().id(1L).build(); // 실제 로그인 사용자
		User anotherUser = User.builder().id(2L).build(); // 리뷰 작성자

		Review review = Review.builder().id(reviewId).reviewer(anotherUser).rating(2).content("초기 내용").build();

		ReviewUpdateRequestDto dto = new ReviewUpdateRequestDto();
		dto.setRating(3);
		dto.setContent("권한 없음");

		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.updateReviewWithImages(reviewId, dto, reviewer, List.of()));
		assertEquals(ErrorCode.REVIEW_UPDATE_FORBIDDEN, ex.getErrorCode());
	}

	@Test
	void 리뷰삭제_성공() {
		// given
		Long reviewId = 100L;
		Long reviewerId = 1L;

		User reviewer = User.builder().id(reviewerId).build();
		Review review = Review.builder().id(reviewId).reviewer(reviewer).build();

		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

		// when
		reviewService.deleteReview(reviewId, reviewer);

		// then
		verify(reviewImageService).deleteImagesByReviewId(reviewId);
		verify(reviewRepository).delete(review);
	}

	@Test
	void 리뷰삭제_실패_리뷰없음() {
		// given
		Long reviewId = 999L;
		given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> reviewService.deleteReview(reviewId, User.builder().id(1L).build()));
		assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void 리뷰삭제_실패_작성자아님() {
		// given
		Long reviewId = 100L;

		User actualReviewer = User.builder().id(1L).build(); // 실제 작성자
		User attacker = User.builder().id(2L).build(); // 삭제 시도자

		Review review = Review.builder().id(reviewId).reviewer(actualReviewer).build();

		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

		// when & then
		GlobalException ex = assertThrows(GlobalException.class, () -> reviewService.deleteReview(reviewId, attacker));
		assertEquals(ErrorCode.REVIEW_DELETE_FORBIDDEN, ex.getErrorCode());
	}

	@Test
	void 받은리뷰목록조회_성공() {
		// given
		Long revieweeId = 2L;
		User reviewee = User.builder().id(revieweeId).build();
		User reviewer = User.builder().id(1L).nickname("작성자").build();
		Product product = Product.builder().id(10L).build();

		Review review = Review.builder().id(100L).product(product).reviewer(reviewer).reviewee(reviewee).rating(4)
				.content("빠른 거래 감사합니다.").build();

		List<String> imageUrls = List.of("https://s3/review-100.jpg");

		given(reviewRepository.findByReviewee(reviewee)).willReturn(List.of(review));
		given(reviewImageService.getImageUrlsByReviewId(100L)).willReturn(imageUrls);

		// when
		List<ReviewResponseDto> result = reviewService.getReviewsReceivedByUser(reviewee);

		// then
		assertEquals(1, result.size());
		ReviewResponseDto dto = result.get(0);
		assertEquals(100L, dto.getReviewId());
		assertEquals("빠른 거래 감사합니다.", dto.getContent());
		assertEquals(4, dto.getRating());
		assertEquals(imageUrls, dto.getImageUrls());

		verify(reviewRepository).findByReviewee(reviewee);
		verify(reviewImageService).getImageUrlsByReviewId(100L);
	}

	@Test
	void 리뷰통계조회_성공() {
	    // given
	    Long productId = 1L;

	    ReviewStatsDto statsDto = ReviewStatsDto.builder()
	            .reviewCount(10L)
	            .averageRating(4.2)
	            .build();

	    given(reviewRepository.findReviewStatsByProductId(productId)).willReturn(statsDto);

	    // when
	    AverageRatingResponseDto result = reviewService.getAverageRatingInfo(productId);

	    // then
	    assertEquals(productId, result.getProductId());
	    assertEquals(4.2, result.getAverageRating());
	    assertEquals(10L, result.getReviewCount());

	    verify(reviewRepository).findReviewStatsByProductId(productId);
	}

}
