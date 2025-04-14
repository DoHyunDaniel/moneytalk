package com.example.moneytalk.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.ProductImage;
import com.example.moneytalk.dto.ProductResponseDto;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{

	List<ProductImage> findByProductId(Long productId);

	List<ProductImage> findByProduct(Product product);

}
