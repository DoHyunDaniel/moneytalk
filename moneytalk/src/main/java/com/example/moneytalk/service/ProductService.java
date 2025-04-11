package com.example.moneytalk.service;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.type.ProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequestDto dto, User user) {
        Product product = Product.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .location(dto.getLocation())
                .status(ProductStatus.SALE) // 기본값
                .build();

        productRepository.save(product);
    }
}
