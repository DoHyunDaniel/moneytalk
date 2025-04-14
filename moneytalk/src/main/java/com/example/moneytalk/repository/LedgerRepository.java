package com.example.moneytalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.User;

public interface LedgerRepository extends JpaRepository<Ledger, Long>{

	boolean existsByUserAndMemoAndAmount(User buyer, String string, int amount);

}
