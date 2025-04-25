package com.example.moneytalk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moneytalk.domain.Budget;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.BudgetRequestDto;
import com.example.moneytalk.dto.BudgetResponseDto;
import com.example.moneytalk.repository.BudgetRepository;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void 예산_신규등록_성공() {
        // given
        User user = User.builder().id(1L).build();

        BudgetRequestDto request = new BudgetRequestDto();
        request.setMonth("2025-04");
        request.setAmount(500000);

        Budget saved = Budget.builder()
                .id(1L)
                .user(user)
                .month("2025-04")
                .amount(500000)
                .build();

        given(budgetRepository.findByUserAndMonth(user, "2025-04")).willReturn(Optional.empty());
        given(budgetRepository.save(any(Budget.class))).willReturn(saved);

        // when
        BudgetResponseDto result = budgetService.saveOrUpdateBudget(user, request);

        // then
        assertThat(result.getMonth()).isEqualTo("2025-04");
        assertThat(result.getAmount()).isEqualTo(500000);
    }

    @Test
    void 예산_중복월_수정_성공() {
        // given
        User user = User.builder().id(1L).build();

        Budget existing = Budget.builder()
                .id(1L)
                .user(user)
                .month("2025-04")
                .amount(400000)
                .build();

        BudgetRequestDto request = new BudgetRequestDto();
        request.setMonth("2025-04");
        request.setAmount(600000); // 수정

        given(budgetRepository.findByUserAndMonth(user, "2025-04")).willReturn(Optional.of(existing));
        given(budgetRepository.save(any(Budget.class))).willReturn(existing);

        // when
        BudgetResponseDto result = budgetService.saveOrUpdateBudget(user, request);

        // then
        assertThat(result.getAmount()).isEqualTo(600000); // 수정됐는지 확인
    }

    @Test
    void 예산_조회_성공() {
        // given
        User user = User.builder().id(1L).build();

        Budget budget = Budget.builder()
                .id(1L)
                .user(user)
                .month("2025-04")
                .amount(450000)
                .build();

        given(budgetRepository.findByUserAndMonth(user, "2025-04"))
                .willReturn(Optional.of(budget));

        // when
        BudgetResponseDto result = budgetService.getBudget(user, "2025-04");

        // then
        assertThat(result.getMonth()).isEqualTo("2025-04");
        assertThat(result.getAmount()).isEqualTo(450000);
    }
}
