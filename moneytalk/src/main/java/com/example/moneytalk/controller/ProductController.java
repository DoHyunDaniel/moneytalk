package com.example.moneytalk.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.dto.ProductSearchRequestDto;
import com.example.moneytalk.dto.ProductStatusUpdateRequestDto;
import com.example.moneytalk.dto.ReviewResponseDto;
import com.example.moneytalk.exception.ErrorResponse;
import com.example.moneytalk.service.FavoriteService;
import com.example.moneytalk.service.ProductImageService;
import com.example.moneytalk.service.ProductService;
import com.example.moneytalk.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


/**
 * ProductController
 * 상품과 관련된 API 요청을 처리하는 컨트롤러입니다.
 *
 * [기능 구성]
 * - 상품 등록, 단건/전체 조회, 검색
 * - 찜하기 기능 (토글/개수 조회)
 * - 리뷰 조회
 * - 이미지 목록 조회
 * - 상품 상태 변경 (예약/판매 완료 등)
 * - 구매 확정 처리
 *
 * [보안]
 * - 등록, 상태 변경, 찜, 구매 확정 등은 JWT 인증 필요
 *
 * @author Daniel
 * @since 2025.04.15
 */

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final FavoriteService favoriteService;
	private final ReviewService reviewService;
	private final ProductImageService productImageService;

	
	// ────────────────── 상품 등록/조회 ──────────────────

	@Operation(summary = "상품 등록", description = "상품 정보와 이미지를 함께 등록합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
	        @ApiResponse(responseCode = "200", description = "상품 등록 성공"),
	        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
	        @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
	})
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> createProduct(
	        @ParameterObject @ModelAttribute @Valid ProductRequestDto request,
	        @Parameter(name = "images", description = "이미지 파일 리스트 (최소 1장)", required = true)
	        @RequestPart("images") List<MultipartFile> images,
	        @AuthenticationPrincipal User user) {
	    productService.createProductWithImages(request, images, user);
	    return ResponseEntity.ok().build();
	}

	@Operation(summary = "상품 상태 변경", description = "상품의 판매 상태(SALE, RESERVED, SOLD)를 변경합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateProductStatus(
	        @Parameter(name = "id", description = "상품 ID", example = "1") @PathVariable("id") Long id,
	        @RequestBody @Valid ProductStatusUpdateRequestDto request,
	        @AuthenticationPrincipal User user) {
	    productService.updateProductStatus(id, request.getStatus(), user);
	    return ResponseEntity.noContent().build();
	}
	
	@Operation(summary = "상품 단건 조회", description = "상품 ID를 기반으로 상세 정보를 조회합니다.")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "상품 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class))),
	        @ApiResponse(responseCode = "400", description = "잘못된 요청 or 상품 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponseDto> getProduct(
	        @Parameter(name = "productId", description = "상품 ID", example = "1", required = true)
	        @PathVariable("productId") Long productId) {
	    return ResponseEntity.ok(productService.getProductById(productId));
	}

	
	// ────────────────── 찜하기 기능 ──────────────────
	
	@Operation(summary = "찜하기 토글", description = "특정 상품을 찜하거나 찜을 취소합니다. (로그인 필요)", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/{id}/favorite")
	public ResponseEntity<String> toggleFavorite(
	        @Parameter(name = "id", description = "상품 ID", example = "1") @PathVariable("id") Long id,
	        @AuthenticationPrincipal User user) {
	    boolean liked = favoriteService.toggleFavorite(id, user);
	    return ResponseEntity.ok(liked ? "찜 추가" : "찜 해제");
	}

	@Operation(summary = "찜 수 조회", description = "특정 상품의 찜한 유저 수를 조회합니다.")
	@GetMapping("/{id}/favorite-count")
	public ResponseEntity<Long> getFavoriteCount(
	        @Parameter(name = "id", description = "상품 ID", example = "1") @PathVariable("id") Long id) {
	    return ResponseEntity.ok(favoriteService.getFavoriteCount(id));
	}	



	// ────────────────── 리뷰, 이미지 조회 ──────────────────
	
	@Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품에 대한 모든 리뷰를 조회합니다.")
	@GetMapping("/{productId}/reviews")
	public ResponseEntity<List<ReviewResponseDto>> getProductReviews(
	        @Parameter(name = "productId", description = "상품 ID", example = "1") @PathVariable Long productId) {
	    List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
	    return ResponseEntity.ok(reviews);
	}

	@Operation(summary = "상품 이미지 목록 조회", description = "특정 상품에 등록된 이미지들을 조회합니다.")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "이미지 목록 조회 성공"),
	        @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음")
	})
	@GetMapping("/{productId}/images")
	public ResponseEntity<List<String>> getProductImages(
	        @Parameter(name = "productId", description = "상품 ID", example = "1") @PathVariable Long productId) {
	    List<String> imageUrls = productImageService.getImageUrlsByProductId(productId);
	    return ResponseEntity.ok(imageUrls);
	}
	
	
	// ────────────────── 검색 및 구매 확정 ──────────────────

	@Operation(summary = "상품 검색", description = """
	        상품 제목, 설명, 카테고리, 지역, 가격 범위, 판매 상태 등의 조건으로 상품을 검색합니다.
	        정렬 옵션은 다음과 같습니다:
	        - `createdAt` (기본값): 최신순 정렬
	        - `price_asc`: 가격 낮은순
	        - `price_desc`: 가격 높은순
	        """)
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "검색 성공"),
	        @ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@GetMapping("/search")
	public ResponseEntity<List<ProductResponseDto>> searchProducts(@ModelAttribute ProductSearchRequestDto request) {
	    List<ProductResponseDto> results = productService.searchProducts(request);
	    return ResponseEntity.ok(results);
	}

	@Operation(summary = "전체 상품 조회", description = "등록된 모든 상품을 최신순으로 조회합니다.")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "조회 성공"),
	        @ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping
	public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
	    List<ProductResponseDto> products = productService.getAllProducts();
	    return ResponseEntity.ok(products);
	}
	
	@Operation(summary = "구매 확정", description = "상품을 구매 확정하고 상태를 SOLD로 변경합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping("/{productId}/confirm")
	public ResponseEntity<Void> confirmPurchase(
	        @Parameter(name = "productId", description = "상품 ID", example = "1") @PathVariable("productId") Long productId,
	        @AuthenticationPrincipal User user) {

	    productService.confirmPurchase(productId, user);
	    return ResponseEntity.noContent().build();
	}


}
