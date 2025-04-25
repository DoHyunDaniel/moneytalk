package com.example.moneytalk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moneytalk.domain.Budget;
import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LedgerMonthlySummaryResponseDto;
import com.example.moneytalk.dto.LedgerRequestDto;
import com.example.moneytalk.dto.LedgerSummaryResponseDto;
import com.example.moneytalk.repository.BudgetRepository;
import com.example.moneytalk.repository.LedgerRepository;
import com.example.moneytalk.type.LedgerType;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;
    
    @Mock
    private BudgetRepository budgetRepository;
    
    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void expense_income_append_success() {
        // given
        User mockUser = User.builder().id(1L).email("test@example.com").build();

        LedgerRequestDto request = new LedgerRequestDto();
        request.setType(LedgerType.EXPENSE);
        request.setAmount(10000);
        request.setCategory("식비");
        request.setMemo("점심값");
        request.setDate(LocalDate.of(2025, 4, 25));
        request.setPaymentMethod("카드");
        request.setTag("식비/점심");

        Ledger savedLedger = Ledger.builder()
                .id(1L)
                .user(mockUser)
                .type(request.getType())
                .amount(request.getAmount())
                .category(request.getCategory())
                .memo(request.getMemo())
                .paymentMethod(request.getPaymentMethod())
                .tag(request.getTag())
                .date(request.getDate())
                .build();

        when(ledgerRepository.save(any(Ledger.class))).thenReturn(savedLedger);

        // when
        Ledger result = ledgerService.registerLedger(mockUser, request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(10000);
        assertThat(result.getCategory()).isEqualTo("식비");
        assertThat(result.getMemo()).isEqualTo("점심값");
        verify(ledgerRepository, times(1)).save(any(Ledger.class));
    }
    
    @Test
    void 월별_지출_내역_정상_조회() {
        // given
        User user = User.builder().id(1L).email("test@example.com").build();

        Ledger ledger1 = Ledger.builder()
                .id(1L)
                .user(user)
                .type(LedgerType.EXPENSE)
                .amount(15000)
                .category("식비")
                .memo("점심")
                .date(LocalDate.of(2025, 4, 10))
                .build();

        Ledger ledger2 = Ledger.builder()
                .id(2L)
                .user(user)
                .type(LedgerType.EXPENSE)
                .amount(25000)
                .category("교통")
                .memo("지하철")
                .date(LocalDate.of(2025, 4, 20))
                .build();

        when(ledgerRepository.findByUserAndDateBetweenOrderByDateDesc(
                eq(user),
                any(LocalDate.class),
                any(LocalDate.class))
        ).thenReturn(List.of(ledger2, ledger1));

        // when
        List<LedgerSummaryResponseDto> result = ledgerService.getLedgerByMonth(user, "2025-04");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L); // 최신순 확인
        assertThat(result.get(1).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAmount()).isEqualTo(25000);
    }
    
    @Test
    void 월간_요약_정상_계산_및_예산_초과_확인() {
        // given
        User user = User.builder().id(1L).email("test@example.com").build();

        List<Ledger> ledgers = List.of(
                Ledger.builder()
                        .id(1L)
                        .user(user)
                        .type(LedgerType.INCOME)
                        .amount(3000000)
                        .category("월급")
                        .date(LocalDate.of(2025, 4, 1))
                        .build(),
                Ledger.builder()
                        .id(2L)
                        .user(user)
                        .type(LedgerType.EXPENSE)
                        .amount(200000)
                        .category("식비")
                        .date(LocalDate.of(2025, 4, 5))
                        .build(),
                Ledger.builder()
                        .id(3L)
                        .user(user)
                        .type(LedgerType.EXPENSE)
                        .amount(250000)
                        .category("쇼핑")
                        .date(LocalDate.of(2025, 4, 10))
                        .build()
        );

        Budget budget = Budget.builder()
                .id(1L)
                .user(user)
                .month("2025-04")
                .amount(400000)
                .build();

        when(ledgerRepository.findByUserAndDateBetween(
                user,
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30))
        ).thenReturn(ledgers);

        when(budgetRepository.findByUserAndMonth(user, "2025-04"))
                .thenReturn(Optional.of(budget));

        // when
        LedgerMonthlySummaryResponseDto result = ledgerService.getMonthlySummary(user, "2025-04");

        // then
        assertThat(result.getTotalIncome()).isEqualTo(3000000);
        assertThat(result.getTotalExpense()).isEqualTo(450000); // 200000 + 250000
        assertThat(result.getBudget()).isEqualTo(400000);
        assertThat(result.isExceeded()).isTrue();

        assertThat(result.getCategorySummary().get("식비")).isEqualTo(200000);
        assertThat(result.getCategorySummary().get("쇼핑")).isEqualTo(250000);
    }
}
