package com.example.moneytalk.dto;

import java.time.LocalDate;

import com.example.moneytalk.type.LedgerType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 수입 또는 지출 항목 등록 요청 DTO입니다.
 * 사용자로부터 가계부 항목 정보를 입력받아 등록 요청 시 사용됩니다.
 */
@Getter
@Setter
@Schema(description = "수입/지출 등록 요청 DTO")
public class LedgerRequestDto {

    @NotNull(message = "수입 또는 지출 구분(type)은 필수입니다.")
    @Schema(description = "항목 유형 (수입: INCOME, 지출: EXPENSE)", example = "EXPENSE", requiredMode = Schema.RequiredMode.REQUIRED)
    private LedgerType type;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 1, message = "금액은 1 이상이어야 합니다.")
    @Schema(description = "항목 금액 (1원 이상)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Schema(description = "지출/수입 카테고리", example = "식비", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @Schema(description = "메모 (선택)", example = "점심 식사")
    private String memo;

    @NotNull(message = "날짜는 필수입니다.")
    @Schema(description = "거래 발생 날짜", example = "2025-04-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "결제 수단 (선택)", example = "신용카드")
    private String paymentMethod;

    @Schema(description = "사용자 정의 태그 (선택)", example = "출장")
    private String tag;
}
