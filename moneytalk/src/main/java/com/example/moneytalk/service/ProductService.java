package com.example.moneytalk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.type.ProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDto createProduct(ProductRequestDto dto, User user) {
        Product product = Product.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .location(dto.getLocation())
                .status(ProductStatus.SALE) // 기본값
                .build();

        Product saved = productRepository.save(product);

        return ProductResponseDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .price(saved.getPrice())
                .category(saved.getCategory())
                .location(saved.getLocation())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .sellerNickname(saved.getUser().getNickname())
                .build();
    }
    
    
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(product -> ProductResponseDto.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .category(product.getCategory())
                        .location(product.getLocation())
                        .status(product.getStatus())
                        .createdAt(product.getCreatedAt())
                        .sellerNickname(product.getUser().getNickname())
                        .build()
                ).collect(Collectors.toList());
    }
    
    public ProductResponseDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));

        return ProductResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .location(product.getLocation())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .sellerNickname(product.getUser().getNickname())
                .build();
    }
    
    @Transactional
    public void updateProductStatus(Long productId, ProductStatus status, User user) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("상품 수정 권한이 없습니다.");
        }

        product.setStatus(status);
    }

}
