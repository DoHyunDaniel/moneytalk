package com.example.moneytalk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.PurchaseHistoryResponseDto;
import com.example.moneytalk.repository.PurchaseHistoryRepository;
import com.example.moneytalk.type.PurchaseType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public List<PurchaseHistoryResponseDto> getHistoriesByType(User user, PurchaseType type) {
        return purchaseHistoryRepository.findByUserAndType(user, type).stream()
                .map(PurchaseHistoryResponseDto::from)
                .toList();
    }
}
