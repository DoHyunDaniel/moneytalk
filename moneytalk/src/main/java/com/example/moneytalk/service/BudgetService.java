package com.example.moneytalk.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.Budget;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.BudgetRequestDto;
import com.example.moneytalk.dto.BudgetResponseDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.BudgetRepository;
import com.example.moneytalk.type.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 예산(Budget) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자의 월별 예산 등록, 수정, 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    /**
     * 사용자의 예산을 저장하거나 수정합니다.
     * 이미 예산이 존재하면 금액을 수정하고, 존재하지 않으면 새로 생성합니다.
     *
     * @param user    예산을 설정할 사용자
     * @param request 예산 등록/수정 요청 DTO
     * @return 저장 또는 수정된 예산 응답 DTO
     */
    @Transactional
    public BudgetResponseDto saveOrUpdateBudget(User user, BudgetRequestDto request) {
        Budget budget = budgetRepository.findByUserAndMonth(user, request.getMonth())
                .map(existing -> {
                    existing.setAmount(request.getAmount());
                    return existing;
                })
                .orElseGet(() -> Budget.builder()
                        .user(user)
                        .month(request.getMonth())
                        .amount(request.getAmount())
                        .build()
                );

        return BudgetResponseDto.from(budgetRepository.save(budget));
    }

    /**
     * 사용자의 특정 월 예산을 조회합니다.
     *
     * @param user  조회 대상 사용자
     * @param month 조회할 월 (yyyy-MM 형식)
     * @return 예산 응답 DTO
     * @throws GlobalException 예산 정보가 존재하지 않을 경우 {@link ErrorCode#BUDGET_NOT_FOUND}
     */
    @Transactional(readOnly = true)
    public BudgetResponseDto getBudget(User user, String month) {
        Budget budget = budgetRepository.findByUserAndMonth(user, month)
                .orElseThrow(() -> new GlobalException(ErrorCode.BUDGET_NOT_FOUND));
        return BudgetResponseDto.from(budget);
    }
}
