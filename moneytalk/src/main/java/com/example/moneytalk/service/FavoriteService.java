package com.example.moneytalk.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.FavoriteProduct;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.FavoriteProductRepository;
import com.example.moneytalk.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteProductRepository favoriteRepo;
    private final ProductRepository productRepo;

    public boolean toggleFavorite(Long productId, User user) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        Optional<FavoriteProduct> existing = favoriteRepo.findByUserAndProduct(user, product);

        if (existing.isPresent()) {
            favoriteRepo.delete(existing.get());
            return false; // 찜 해제
        } else {
            favoriteRepo.save(FavoriteProduct.builder()
                    .user(user)
                    .product(product)
                    .build());
            return true; // 찜 추가
        }
    }

    public long getFavoriteCount(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        return favoriteRepo.countByProduct(product);
    }
}
