package com.example.moneytalk.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.User;

public interface LedgerRepository extends JpaRepository<Ledger, Long>{

	boolean existsByUserAndMemoAndAmount(User buyer, String string, int amount);

	List<Ledger> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

	List<Ledger> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

}
