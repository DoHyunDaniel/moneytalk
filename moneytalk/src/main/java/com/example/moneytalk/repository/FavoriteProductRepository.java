package com.example.moneytalk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.FavoriteProduct;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

	boolean existsByUserAndProduct(User user, Product product);

	Optional<FavoriteProduct> findByUserAndProduct(User user, Product product);

	Long countByProduct(Product product);
}
