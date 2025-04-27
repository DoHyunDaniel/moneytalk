package com.example.moneytalk.dto;

import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.type.LedgerType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 수입/지출 항목의 요약 정보를 담은 DTO입니다.
 * 월별 가계부 리스트 등에서 사용됩니다.
 */
@Getter
@Builder
@Schema(description = "가계부 항목 요약 DTO")
public class LedgerSummaryResponseDto {

    @Schema(description = "가계부 항목 ID", example = "1")
    private final Long id;

    @Schema(description = "항목 유형 (수입: INCOME, 지출: EXPENSE)", example = "INCOME")
    private final LedgerType type;

    @Schema(description = "항목 금액", example = "25000")
    private final Integer amount;

    @Schema(description = "카테고리", example = "교통")
    private final String category;

    @Schema(description = "메모 (선택)", example = "지하철 정기권")
    private final String memo;

    @Schema(description = "거래 날짜", example = "2025-04-20")
    private final LocalDate date;

    /**
     * Ledger 엔티티로부터 LedgerSummaryResponseDto로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param ledger Ledger 엔티티
     * @return LedgerSummaryResponseDto 인스턴스
     */
    public static LedgerSummaryResponseDto from(Ledger ledger) {
        return LedgerSummaryResponseDto.builder()
                .id(ledger.getId())
                .type(ledger.getType())
                .amount(ledger.getAmount())
                .category(ledger.getCategory())
                .memo(ledger.getMemo())
                .date(ledger.getDate())
                .build();
    }
}
