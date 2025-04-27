package com.example.moneytalk.dto;

import com.example.moneytalk.domain.Budget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 예산 정보 응답 DTO입니다.
 * 특정 월에 설정된 예산 금액 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "예산 응답 DTO")
public class BudgetResponseDto {

    @Schema(description = "예산이 설정된 월 (yyyy-MM 형식)", example = "2025-04")
    private final String month;

    @Schema(description = "설정된 예산 금액", example = "400000")
    private final Integer amount;
    
    /**
     * Budget 엔티티로부터 BudgetResponseDto로 변환하는 정적 팩토리 메서드
     *
     * @param budget Budget 엔티티
     * @return BudgetResponseDto 인스턴스
     */
    public static BudgetResponseDto from(Budget budget) {
        return BudgetResponseDto.builder()
                .month(budget.getMonth())
                .amount(budget.getAmount())
                .build();
    }
}
