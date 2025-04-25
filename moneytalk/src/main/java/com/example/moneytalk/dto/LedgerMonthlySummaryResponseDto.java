package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 월간 가계부 요약 정보를 담은 DTO입니다.
 * 총 수입, 총 지출, 카테고리별 지출 합계, 예산, 예산 초과 여부 등을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "월간 가계부 요약 응답 DTO")
public class LedgerMonthlySummaryResponseDto {

    @Schema(description = "해당 월의 총 수입 합계", example = "3000000")
    private Integer totalIncome;

    @Schema(description = "해당 월의 총 지출 합계", example = "450000")
    private Integer totalExpense;

    @Schema(description = "카테고리별 지출 합계 (예: { \"식비\": 200000, \"쇼핑\": 250000 })")
    private Map<String, Integer> categorySummary;

    @Schema(description = "설정된 예산 금액", example = "400000")
    private Integer budget;

    @Schema(description = "예산 초과 여부", example = "true")
    private boolean isExceeded;
}
