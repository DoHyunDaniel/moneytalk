package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.PurchaseHistoryResponseDto;
import com.example.moneytalk.repository.PurchaseHistoryRepository;
import com.example.moneytalk.type.PurchaseType;

import lombok.RequiredArgsConstructor;

/**
 * PurchaseHistoryService
 * 사용자의 상품 구매/판매 이력을 조회하는 서비스입니다.
 *
 * [기능 설명]
 * - 사용자와 이력 타입(PURCHASE / SALE)을 기준으로 거래 내역을 조회합니다.
 * - 반환된 거래 내역은 응답 DTO로 변환되어 클라이언트에 제공됩니다.
 *
 * [관련 타입]
 * - {@link PurchaseType}: PURCHASE(구매), SALE(판매)
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    /**
     * 사용자와 거래 타입(PURCHASE / SALE)에 따라 거래 이력을 조회합니다.
     *
     * @param user 조회 대상 사용자
     * @param type 거래 유형 (구매 또는 판매)
     * @return 거래 이력 응답 DTO 리스트
     */
    public List<PurchaseHistoryResponseDto> getHistoriesByType(User user, PurchaseType type) {
        return purchaseHistoryRepository.findByUserAndType(user, type).stream()
                .map(PurchaseHistoryResponseDto::from)
                .toList();
    }
}

