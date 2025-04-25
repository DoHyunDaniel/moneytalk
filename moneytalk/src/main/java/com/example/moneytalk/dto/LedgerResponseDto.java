package com.example.moneytalk.dto;

import java.time.LocalDate;

import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.type.LedgerType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 단일 가계부 항목 응답 DTO입니다.
 * 수입/지출 등록 및 조회 시 클라이언트에 반환되는 응답 데이터 구조를 정의합니다.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가계부 항목 응답 DTO")
public class LedgerResponseDto {

    @Schema(description = "가계부 항목 ID", example = "1")
    private Long id;

    @Schema(description = "항목 유형 (수입: INCOME, 지출: EXPENSE)", example = "EXPENSE")
    private LedgerType type;

    @Schema(description = "항목 금액", example = "15000")
    private Integer amount;

    @Schema(description = "카테고리", example = "식비")
    private String category;

    @Schema(description = "메모", example = "점심 식사")
    private String memo;

    @Schema(description = "거래 날짜", example = "2025-04-15")
    private LocalDate date;

    /**
     * Ledger 엔티티로부터 LedgerResponseDto로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param ledger Ledger 엔티티
     * @return LedgerResponseDto 인스턴스
     */
    public static LedgerResponseDto from(Ledger ledger) {
        return LedgerResponseDto.builder()
                .id(ledger.getId())
                .type(ledger.getType())
                .amount(ledger.getAmount())
                .category(ledger.getCategory())
                .memo(ledger.getMemo())
                .date(ledger.getDate())
                .build();
    }
}
