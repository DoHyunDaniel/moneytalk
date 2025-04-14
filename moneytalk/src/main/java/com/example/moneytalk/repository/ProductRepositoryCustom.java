package com.example.moneytalk.repository;

import java.util.List;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.dto.ProductSearchRequest;

public interface ProductRepositoryCustom {
    List<Product> searchByConditions(ProductSearchRequest request);
}
