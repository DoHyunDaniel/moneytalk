package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.AverageRatingResponseDto;
import com.example.moneytalk.dto.ReviewRequestDto;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.dto.ReviewUpdateRequestDto;
import com.example.moneytalk.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(summary = "리뷰 작성", description = "상품 거래가 완료된 후, 판매자 또는 구매자에게 리뷰를 작성합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "리뷰 작성 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					    {
					      "timestamp": "2025-04-11T14:15:00",
					      "status": 400,
					      "error": "Bad Request",
					      "message": "해당 상품에 대한 리뷰를 작성할 수 없습니다.",
					      "path": "/api/reviews"
					    }
					"""))),
			@ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음 또는 만료됨)") })
	
	@PostMapping
	public ResponseEntity<Void> writeReview(@AuthenticationPrincipal User reviewer,
			@RequestBody @Valid ReviewRequestDto dto) {
		reviewService.writeReview(dto, reviewer);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{reviewId}")
	@Operation(summary = "리뷰 수정", description = "리뷰 작성자가 본인의 리뷰를 수정합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> updateReview(@PathVariable Long reviewId,
	                                         @AuthenticationPrincipal User reviewer,
	                                         @RequestBody @Valid ReviewUpdateRequestDto dto) {
	    reviewService.updateReview(reviewId, dto, reviewer);
	    return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{reviewId}")
	@Operation(summary = "리뷰 삭제", description = "리뷰 작성자가 본인의 리뷰를 삭제합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
	                                         @AuthenticationPrincipal User reviewer) {
	    reviewService.deleteReview(reviewId, reviewer);
	    return ResponseEntity.noContent().build();
	}

	@Operation(summary = "내가 받은 리뷰 목록 조회", description = "로그인한 사용자가 받은 모든 리뷰를 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping("/received")
	public ResponseEntity<List<ReviewResponseDto>> getReceivedReviews(@AuthenticationPrincipal User user) {
	    List<ReviewResponseDto> reviews = reviewService.getReviewsReceivedByUser(user);
	    return ResponseEntity.ok(reviews);
	}

	@Operation(summary = "상품 평균 평점 조회", description = "상품 ID에 대한 평균 평점과 리뷰 수를 반환합니다.")
	@GetMapping("/products/{productId}/average-rating")
	public ResponseEntity<AverageRatingResponseDto> getAverageRating(@PathVariable Long productId) {
	    return ResponseEntity.ok(reviewService.getAverageRatingInfo(productId));
	}

}
