package com.example.moneytalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.BudgetRequestDto;
import com.example.moneytalk.dto.BudgetResponseDto;
import com.example.moneytalk.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 예산(Budget) 관련 API를 처리하는 컨트롤러입니다.
 * 사용자는 특정 월의 예산을 등록하거나 조회할 수 있습니다.
 */
@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@Tag(name = "예산 API", description = "월별 예산 등록 및 조회 기능 제공")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 예산 등록 또는 수정 API
     *
     * @param user    인증된 사용자
     * @param request 예산 등록/수정 요청 데이터
     * @return 등록/수정된 예산 응답
     */
    @PostMapping
    @Operation(summary = "예산 등록/수정", description = "사용자의 특정 월 예산을 등록하거나 수정합니다.")
    public ResponseEntity<BudgetResponseDto> saveOrUpdateBudget(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Valid @RequestBody BudgetRequestDto request
    ) {
        BudgetResponseDto response = budgetService.saveOrUpdateBudget(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 월의 예산 조회 API
     *
     * @param user  인증된 사용자
     * @param month 조회할 월 (yyyy-MM 형식)
     * @return 해당 월의 예산 정보
     */
    @GetMapping
    @Operation(summary = "예산 조회", description = "사용자의 특정 월 예산 정보를 조회합니다.")
    public ResponseEntity<BudgetResponseDto> getBudget(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2025-04") @RequestParam("month") String month
    ) {
        BudgetResponseDto response = budgetService.getBudget(user, month);
        return ResponseEntity.ok(response);
    }
}
