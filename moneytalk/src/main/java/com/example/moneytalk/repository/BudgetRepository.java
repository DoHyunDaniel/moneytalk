package com.example.moneytalk.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Budget;
import com.example.moneytalk.domain.User;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserAndMonth(User user, String month);
}
