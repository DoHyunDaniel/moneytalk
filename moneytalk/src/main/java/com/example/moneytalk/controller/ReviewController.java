package com.example.moneytalk.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.AverageRatingResponseDto;
import com.example.moneytalk.dto.ReviewRequestDto;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.dto.ReviewUpdateRequestDto;
import com.example.moneytalk.service.ReviewImageService;
import com.example.moneytalk.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ReviewController 리뷰(후기) 및 리뷰 이미지 관련 API를 제공하는 컨트롤러입니다.
 *
 * [기능 설명] - 리뷰 작성, 수정, 삭제 - 리뷰 단건 및 받은 리뷰 목록 조회 - 리뷰 이미지 업로드 및 조회 - 상품의 평균 평점 및
 * 리뷰 수 조회
 *
 * [보안] - 리뷰 작성/수정/삭제/이미지 업로드는 JWT 인증 필요
 *
 * [관련 대상] - 구매자만 리뷰 작성 가능 - 리뷰는 상품당 1회 작성 가능 - 본인의 리뷰만 수정/삭제 가능
 *
 * @author Daniel
 * @since 2025.04.15
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;
	private final ReviewImageService reviewImageService;

	
	// ───────────────────── 리뷰 등록/수정/삭제 ─────────────────────

	@Operation(summary = "리뷰 작성 + 이미지 업로드", description = "리뷰를 작성하면서 이미지도 함께 업로드합니다.", security = @SecurityRequirement(name = "bearerAuth"))
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
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> writeReview(@AuthenticationPrincipal User reviewer,
			@ParameterObject @ModelAttribute @Valid ReviewRequestDto dto,
			@Parameter(name = "images", description = "이미지 파일 리스트 (최소 1장)", required = true) @RequestPart("images") List<MultipartFile> images) {

		reviewService.writeReviewWithImages(dto, images, reviewer);
		return ResponseEntity.ok().build();
	}

	@PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "리뷰 수정 + 이미지 교체", description = "리뷰 내용과 이미지를 수정합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> updateReview(@PathVariable("reviewId") Long reviewId,
			@AuthenticationPrincipal User reviewer, @ParameterObject @ModelAttribute @Valid ReviewUpdateRequestDto dto,
			@Parameter(name = "images", description = "이미지 파일 리스트 (최소 1장)", required = true) @RequestPart("images") List<MultipartFile> images) {

		reviewService.updateReviewWithImages(reviewId, dto, reviewer, images);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{reviewId}")
	@Operation(summary = "리뷰 삭제", description = "리뷰 작성자가 본인의 리뷰를 삭제합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId,
			@AuthenticationPrincipal User reviewer) {
		reviewService.deleteReview(reviewId, reviewer);
		return ResponseEntity.noContent().build();
	}

	
	// ───────────────────── 리뷰 조회 기능 ─────────────────────
	
	@Operation(summary = "내가 받은 리뷰 목록 조회", description = "로그인한 사용자가 받은 모든 리뷰를 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping("/received")
	public ResponseEntity<List<ReviewResponseDto>> getReceivedReviews(@AuthenticationPrincipal User user) {
		List<ReviewResponseDto> reviews = reviewService.getReviewsReceivedByUser(user);
		return ResponseEntity.ok(reviews);
	}

	@GetMapping("/{reviewId}")
	@Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
	public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable("reviewId") Long reviewId) {
		ReviewResponseDto dto = reviewService.getReviewById(reviewId);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "상품 평균 평점 조회", description = "상품 ID에 대한 평균 평점과 리뷰 수를 반환합니다.")
	@GetMapping("/products/{productId}/average-rating")
	public ResponseEntity<AverageRatingResponseDto> getAverageRating(@PathVariable("productId") Long productId) {
		return ResponseEntity.ok(reviewService.getAverageRatingInfo(productId));
	}

	
	// ───────────────────── 리뷰 이미지 업로드/조회 ─────────────────────
	
	@Operation(summary = "리뷰 이미지 업로드", description = "리뷰 작성 이후 이미지 파일들을 업로드합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "200", description = "업로드 성공, 이미지 URL 리스트 반환")
	@PostMapping(value = "/{reviewId}/images", consumes = "multipart/form-data")
	public ResponseEntity<List<String>> uploadReviewImages(@PathVariable("reviewId") Long reviewId,
			@RequestPart("images") List<MultipartFile> images, @AuthenticationPrincipal User user) {

		List<String> urls = reviewImageService.uploadReviewImages(reviewId, images);
		return ResponseEntity.ok(urls);
	}

	@Operation(summary = "리뷰 이미지 목록 조회", description = "특정 리뷰에 등록된 이미지 URL 목록을 반환합니다.")
	@ApiResponse(responseCode = "200", description = "이미지 목록 반환")
	@GetMapping("/{reviewId}/images")
	public ResponseEntity<List<String>> getReviewImages(@PathVariable("reviewId") Long reviewId) {
		List<String> urls = reviewImageService.getImageUrlsByReviewId(reviewId);
		return ResponseEntity.ok(urls);
	}

}
