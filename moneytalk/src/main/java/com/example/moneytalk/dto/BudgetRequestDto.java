package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 예산 등록 또는 수정 요청을 위한 DTO입니다.
 * 사용자는 특정 월(month)에 대한 예산 금액(amount)을 설정할 수 있습니다.
 */
@Getter
@Setter
@Schema(description = "예산 등록/수정 요청 DTO")
public class BudgetRequestDto {

    @NotBlank(message = "월(month)은 필수 항목입니다.")
    @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$", message = "yyyy-MM 형식이어야 합니다.")
    @Schema(description = "예산을 등록할 월 (yyyy-MM 형식)", example = "2025-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private String month;

    @NotNull(message = "예산 금액은 필수 항목입니다.")
    @Min(value = 0, message = "예산 금액은 0 이상이어야 합니다.")
    @Schema(description = "해당 월의 예산 금액 (0 이상의 정수)", example = "400000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;
}
