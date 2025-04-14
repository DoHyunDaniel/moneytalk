package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.moneytalk.repository.ProductImageRepository;
import com.example.moneytalk.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public List<String> getImageUrlsByProductId(Long productId) {
        // 상품 존재 여부 확인
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        }

        return productImageRepository.findByProductId(productId)
                .stream()
                .map(image -> image.getImageUrl())
                .toList();
    }
}
