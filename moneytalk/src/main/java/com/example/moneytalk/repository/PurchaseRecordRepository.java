package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.PurchaseRecord;
import com.example.moneytalk.domain.User;

public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Long> {
	boolean existsByProduct(Product product);

	List<PurchaseRecord> findByBuyer(User buyer);

	boolean existsByBuyerAndProduct(User buyer, Product product);
}
