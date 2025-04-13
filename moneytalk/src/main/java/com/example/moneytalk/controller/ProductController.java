package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.dto.ProductStatusUpdateRequest;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.exception.ErrorResponse;
import com.example.moneytalk.service.FavoriteService;
import com.example.moneytalk.service.ProductService;
import com.example.moneytalk.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final FavoriteService favoriteService;
	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Valid ProductRequestDto dto,
			@AuthenticationPrincipal User user) {
		ProductResponseDto response = productService.createProduct(dto, user);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "전체 상품 조회", description = "등록된 모든 상품을 최신순으로 조회합니다.")
	@GetMapping
	public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@Operation(summary = "상품 단건 조회", description = "상품 ID를 기반으로 상세 정보를 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "상품 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class), examples = @ExampleObject(value = """
					    {
					      "id": 1,
					      "title": "맥북 M1",
					      "description": "사용감 거의 없는 맥북 팝니다",
					      "price": 1200000,
					      "category": "전자기기",
					      "location": "서울 강남구",
					      "status": "SALE",
					      "createdAt": "2025-04-11T12:34:56",
					      "sellerNickname": "홍길동"
					    }
					"""))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 or 상품 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = """
					    {
					      "timestamp": "2025-04-11T13:00:00",
					      "status": 400,
					      "error": "Bad Request",
					      "message": "해당 상품을 찾을 수 없습니다.",
					      "path": "/api/products/999"
					    }
					"""))) })
	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
		return ResponseEntity.ok(productService.getProductById(productId));
	}

	@Operation(summary = "찜하기 토글", description = "특정 상품을 찜하거나 찜을 취소합니다. (로그인 필요)", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/{id}/favorite")
	public ResponseEntity<String> toggleFavorite(@Parameter(description = "상품 ID", example = "1") @PathVariable Long id,
			@AuthenticationPrincipal User user) {

		boolean liked = favoriteService.toggleFavorite(id, user);
		return ResponseEntity.ok(liked ? "찜 추가" : "찜 해제");
	}

	@Operation(summary = "찜 수 조회", description = "특정 상품의 찜한 유저 수를 조회합니다.")
	@GetMapping("/{id}/favorite-count")
	public ResponseEntity<Long> getFavoriteCount(
			@Parameter(description = "상품 ID", example = "1") @PathVariable Long id) {

		return ResponseEntity.ok(favoriteService.getFavoriteCount(id));
	}

	@Operation(summary = "상품 상태 변경", description = "상품의 판매 상태(SALE, RESERVED, SOLD)를 변경합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateProductStatus(@PathVariable Long id,
			@RequestBody @Valid ProductStatusUpdateRequest request, @AuthenticationPrincipal User user) {

		productService.updateProductStatus(id, request.getStatus(), user);
		return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품에 대한 모든 리뷰를 조회합니다.")
	@GetMapping("/{productId}/reviews")
	public ResponseEntity<List<ReviewResponseDto>> getProductReviews(@PathVariable Long productId) {
		List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
		return ResponseEntity.ok(reviews);
	}
}
