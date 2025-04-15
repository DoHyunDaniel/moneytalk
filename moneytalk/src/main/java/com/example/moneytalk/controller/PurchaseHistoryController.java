package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.PurchaseHistoryResponseDto;
import com.example.moneytalk.service.PurchaseHistoryService;
import com.example.moneytalk.type.PurchaseType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;


/**
 * PurchaseHistoryController
 * 사용자의 구매 및 판매 이력 조회 API를 제공합니다.
 *
 * [기능 설명]
 * - 로그인한 사용자의 구매 이력을 조회
 * - 로그인한 사용자의 판매 이력을 조회
 *
 * [보안]
 * - 모든 요청은 JWT 인증을 기반으로 합니다. (@AuthenticationPrincipal 사용)
 *
 * @author Daniel
 * @since 2025.04.15
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/purchase-history")
@RequiredArgsConstructor
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;

    /**
     * 로그인한 사용자의 구매 이력을 조회합니다.
     *
     * @param user 인증된 사용자
     * @return 구매 이력 리스트
     */
    @Operation(summary = "내 구매 내역 조회")
    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseHistoryResponseDto>> getMyPurchases(@AuthenticationPrincipal User user) {
        List<PurchaseHistoryResponseDto> purchases = purchaseHistoryService.getHistoriesByType(user, PurchaseType.PURCHASE);
        return ResponseEntity.ok(purchases);
    }

    /**
     * 로그인한 사용자의 판매 이력을 조회합니다.
     *
     * @param user 인증된 사용자
     * @return 판매 이력 리스트
     */
    @Operation(summary = "내 판매 내역 조회")
    @GetMapping("/sales")
    public ResponseEntity<List<PurchaseHistoryResponseDto>> getMySales(@AuthenticationPrincipal User user) {
        List<PurchaseHistoryResponseDto> sales = purchaseHistoryService.getHistoriesByType(user, PurchaseType.SALE);
        return ResponseEntity.ok(sales);
    }
}
