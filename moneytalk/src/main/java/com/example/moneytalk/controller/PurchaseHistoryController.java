package com.example.moneytalk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.PurchaseHistoryResponseDto;
import com.example.moneytalk.service.PurchaseHistoryService;
import com.example.moneytalk.type.PurchaseType;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-history")
@RequiredArgsConstructor
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;

    @Operation(summary = "내 구매 내역 조회")
    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseHistoryResponseDto>> getMyPurchases(@AuthenticationPrincipal User user) {
        List<PurchaseHistoryResponseDto> purchases = purchaseHistoryService.getHistoriesByType(user, PurchaseType.PURCHASE);
        return ResponseEntity.ok(purchases);
    }

    @Operation(summary = "내 판매 내역 조회")
    @GetMapping("/sales")
    public ResponseEntity<List<PurchaseHistoryResponseDto>> getMySales(@AuthenticationPrincipal User user) {
        List<PurchaseHistoryResponseDto> sales = purchaseHistoryService.getHistoriesByType(user, PurchaseType.SALE);
        return ResponseEntity.ok(sales);
    }
}
