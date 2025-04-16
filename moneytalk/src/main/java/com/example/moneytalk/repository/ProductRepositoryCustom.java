package com.example.moneytalk.repository;

import java.util.List;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.dto.ProductSearchRequestDto;

public interface ProductRepositoryCustom {
    List<Product> searchByConditions(ProductSearchRequestDto request);
}
