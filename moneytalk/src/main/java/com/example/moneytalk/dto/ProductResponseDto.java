package com.example.moneytalk.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.type.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 단건 조회 및 목록 응답 DTO입니다.
 * 상품 정보, 판매자 정보, 이미지 URL 목록 등을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "상품 상세 조회 응답 DTO")
public class ProductResponseDto {

    @Schema(description = "상품 ID", example = "101")
    private Long id;

    @Schema(description = "상품 제목", example = "아이폰 14 Pro 미개봉")
    private String title;

    @Schema(description = "상품 설명", example = "박스 개봉만 한 새 제품입니다. 구성품 모두 포함.")
    private String description;

    @Schema(description = "상품 가격 (단위: 원)", example = "1250000")
    private Integer price;

    @Schema(description = "상품 카테고리", example = "전자기기")
    private String category;

    @Schema(description = "상품 거래 위치", example = "서울시 강남구 역삼동")
    private String location;

    @Schema(description = "상품 상태", example = "SALE")
    private ProductStatus status;

    @Schema(description = "상품 등록 시간", example = "2025-04-25T16:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "판매자 ID", example = "5")
    private Long sellerId;

    @Schema(description = "판매자 닉네임", example = "dohyunnn")
    private String sellerNickname;

    @Schema(description = "상품 이미지 URL 목록", example = "[\"https://.../1.jpg\", \"https://.../2.jpg\"]")
    private List<String> images;

    /**
     * Product 엔티티와 이미지 URL 목록으로부터 ProductResponseDto를 생성합니다.
     *
     * @param product 상품 엔티티
     * @param imageUrls 상품에 연결된 이미지 URL 목록
     * @return ProductResponseDto 인스턴스
     */
    public static ProductResponseDto from(Product product, List<String> imageUrls) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .location(product.getLocation())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .sellerId(product.getUser().getId())
                .sellerNickname(product.getUser().getNickname())
                .images(imageUrls)
                .build();
    }
}
