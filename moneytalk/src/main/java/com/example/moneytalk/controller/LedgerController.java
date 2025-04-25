package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LedgerMonthlySummaryResponseDto;
import com.example.moneytalk.dto.LedgerRequestDto;
import com.example.moneytalk.dto.LedgerResponseDto;
import com.example.moneytalk.dto.LedgerSummaryResponseDto;
import com.example.moneytalk.service.LedgerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 가계부(Ledger) 관련 API를 제공하는 컨트롤러입니다.
 * 수입/지출 등록, 월별 내역 조회, 월간 요약 기능을 포함합니다.
 */
@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
@Tag(name = "가계부 API", description = "수입/지출 등록, 월별 내역 및 요약 조회 API")
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 가계부 항목 등록 API
     *
     * @param user       인증된 사용자
     * @param requestDto 수입 또는 지출 항목 등록 요청
     * @return 등록된 항목 정보
     */
    @PostMapping
    @Operation(summary = "가계부 항목 등록", description = "사용자의 수입 또는 지출 내역을 등록합니다.")
    public ResponseEntity<LedgerResponseDto> registerLedger(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Valid @RequestBody LedgerRequestDto requestDto
    ) {
        Ledger saved = ledgerService.registerLedger(user, requestDto);
        return ResponseEntity.ok(LedgerResponseDto.from(saved));
    }

    /**
     * 월별 가계부 내역 조회 API
     *
     * @param user  인증된 사용자
     * @param month 조회할 월 (yyyy-MM 형식)
     * @return 월별 수입/지출 내역 리스트
     */
    @GetMapping
    @Operation(summary = "월별 수입/지출 내역 조회", description = "사용자의 특정 월 수입 및 지출 내역을 조회합니다.")
    public ResponseEntity<List<LedgerSummaryResponseDto>> getLedgerList(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2025-04") @RequestParam("month") String month
    ) {
        List<LedgerSummaryResponseDto> result = ledgerService.getLedgerByMonth(user, month);
        return ResponseEntity.ok(result);
    }

    /**
     * 월간 가계부 요약 정보 조회 API
     *
     * @param user  인증된 사용자
     * @param month 요약을 조회할 월 (yyyy-MM 형식)
     * @return 총 수입, 지출, 예산 초과 여부, 카테고리별 지출 합계 등
     */
    @GetMapping("/summary")
    @Operation(summary = "월간 요약 조회", description = "해당 월의 총 수입, 지출, 예산 초과 여부 및 카테고리별 지출 합계를 요약해서 반환합니다.")
    public ResponseEntity<LedgerMonthlySummaryResponseDto> getMonthlySummary(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2025-04") @RequestParam("month") String month
    ) {
        LedgerMonthlySummaryResponseDto summary = ledgerService.getMonthlySummary(user, month);
        return ResponseEntity.ok(summary);
    }
}
