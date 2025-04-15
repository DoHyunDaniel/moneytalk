package com.example.moneytalk.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.FavoriteProduct;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.FavoriteProductRepository;
import com.example.moneytalk.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * FavoriteService
 * 상품에 대한 찜하기(좋아요) 기능을 처리하는 서비스입니다.
 *
 * [기능 설명]
 * - 사용자가 상품을 찜하거나 찜 해제할 수 있습니다. (toggle 방식)
 * - 특정 상품의 찜 개수를 조회할 수 있습니다.
 *
 * [주요 메서드]
 * - toggleFavorite(): 찜 여부 토글 (있으면 삭제, 없으면 추가)
 * - getFavoriteCount(): 해당 상품의 찜 개수 반환
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteProductRepository favoriteRepo;
    private final ProductRepository productRepo;

    /**
     * 사용자의 찜 상태를 토글합니다.
     * - 이미 찜한 경우: 찜 해제 (삭제)
     * - 찜하지 않은 경우: 찜 추가 (등록)
     *
     * @param productId 찜할 상품 ID
     * @param user 요청 사용자
     * @return true = 찜 추가, false = 찜 해제
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우
     */
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

    /**
     * 특정 상품에 대해 찜(좋아요)을 누른 사용자 수를 반환합니다.
     *
     * @param productId 상품 ID
     * @return 찜 개수 (long)
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우
     */
    public long getFavoriteCount(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        return favoriteRepo.countByProduct(product);
    }
}
