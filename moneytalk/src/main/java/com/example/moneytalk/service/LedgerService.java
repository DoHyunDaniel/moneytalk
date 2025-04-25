package com.example.moneytalk.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.Budget;
import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LedgerMonthlySummaryResponseDto;
import com.example.moneytalk.dto.LedgerRequestDto;
import com.example.moneytalk.dto.LedgerSummaryResponseDto;
import com.example.moneytalk.repository.BudgetRepository;
import com.example.moneytalk.repository.LedgerRepository;
import com.example.moneytalk.type.LedgerType;

import lombok.RequiredArgsConstructor;

/**
 * 가계부(Ledger) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 항목 등록, 월별 조회, 월간 요약 등 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final BudgetRepository budgetRepository;

    /**
     * 사용자의 수입 또는 지출 항목을 등록합니다.
     *
     * @param user    등록할 사용자
     * @param request 등록 요청 DTO
     * @return 저장된 Ledger 엔티티
     */
    public Ledger registerLedger(User user, LedgerRequestDto request) {
        Ledger ledger = Ledger.builder()
                .user(user)
                .type(request.getType())
                .amount(request.getAmount())
                .category(request.getCategory())
                .memo(request.getMemo())
                .paymentMethod(request.getPaymentMethod())
                .tag(request.getTag())
                .date(request.getDate())
                .build();

        return ledgerRepository.save(ledger);
    }

    /**
     * 특정 월에 해당하는 사용자의 수입/지출 항목을 최신순으로 조회합니다.
     *
     * @param user  조회할 사용자
     * @param month 조회할 월 (yyyy-MM 형식)
     * @return LedgerSummaryResponseDto 리스트
     */
    public List<LedgerSummaryResponseDto> getLedgerByMonth(User user, String month) {
        YearMonth yearMonth = YearMonth.parse(month); // 예: 2025-04
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Ledger> ledgers = ledgerRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end);

        return ledgers.stream()
                .map(LedgerSummaryResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 월 가계부 데이터를 기반으로 수입, 지출, 예산 초과 여부 등의 요약 정보를 반환합니다.
     *
     * @param user      조회할 사용자
     * @param monthStr  조회할 월 (yyyy-MM 형식)
     * @return LedgerMonthlySummaryResponseDto
     */
    public LedgerMonthlySummaryResponseDto getMonthlySummary(User user, String monthStr) {
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Ledger> ledgers = ledgerRepository.findByUserAndDateBetween(user, start, end);

        int totalIncome = ledgers.stream()
                .filter(l -> l.getType() == LedgerType.INCOME)
                .mapToInt(Ledger::getAmount)
                .sum();

        int totalExpense = ledgers.stream()
                .filter(l -> l.getType() == LedgerType.EXPENSE)
                .mapToInt(Ledger::getAmount)
                .sum();

        // 카테고리별 지출 합계
        Map<String, Integer> categorySummary = ledgers.stream()
                .filter(l -> l.getType() == LedgerType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Ledger::getCategory,
                        Collectors.summingInt(Ledger::getAmount)
                ));

        // 예산 조회 및 초과 여부 판단
        Optional<Budget> budgetOpt = budgetRepository.findByUserAndMonth(user, monthStr);
        int budget = budgetOpt.map(Budget::getAmount).orElse(0);
        boolean isExceeded = totalExpense > budget;

        return LedgerMonthlySummaryResponseDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .categorySummary(categorySummary)
                .budget(budget)
                .isExceeded(isExceeded)
                .build();
    }
}
