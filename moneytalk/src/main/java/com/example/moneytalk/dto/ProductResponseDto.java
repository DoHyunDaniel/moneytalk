package com.example.moneytalk.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.type.ProductStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {
	private Long id;
	private String title;
	private String description;
	private Integer price;
	private String category;
	private String location;
	private ProductStatus status;
	private LocalDateTime createdAt;
	private Long sellerId;
	private String sellerNickname; // 유저 정보 일부도 포함
	private List<String> images;

	public static ProductResponseDto from(Product product, List<String> imageUrls) {
		return ProductResponseDto.builder().id(product.getId()).title(product.getTitle())
				.sellerId(product.getUser().getId()).description(product.getDescription()).price(product.getPrice())
				.category(product.getCategory()).location(product.getLocation()).status(product.getStatus())
				.createdAt(product.getCreatedAt()).sellerNickname(product.getUser().getNickname()).images(imageUrls)
				.build();
	}

}
