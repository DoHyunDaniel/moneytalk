package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.dto.ProductSearchRequestDto;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

	List<Product> findAllByOrderByCreatedAtDesc();

	List<Product> searchByConditions(ProductSearchRequestDto request);
}
