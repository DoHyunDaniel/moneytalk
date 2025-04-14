package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.PurchaseHistory;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.type.PurchaseType;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    List<PurchaseHistory> findByUserAndType(User user, PurchaseType type);

	boolean existsByUserAndProductAndType(User buyer, Product product, PurchaseType purchase);
}
