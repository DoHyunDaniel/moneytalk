package com.example.moneytalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatbotSummaryResponseDto;
import com.example.moneytalk.service.ChatbotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * 소비 요약 챗봇 관련 API를 제공하는 컨트롤러입니다.
 * 사용자의 월별 소비 데이터를 기반으로 요약 정보를 반환합니다.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Tag(name = "소비 요약 챗봇 API", description = "소비 통계 기반 챗봇 응답 API")
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 월별 소비 요약 조회 API
     *
     * @param user  인증된 사용자 정보
     * @param month 요약을 조회할 월 (yyyy-MM 형식)
     * @return 소비 요약 응답 객체
     */
    @GetMapping("/summary")
    @Operation(summary = "소비 요약 조회", description = "사용자의 월별 소비 데이터를 요약하여 반환합니다.")
    public ResponseEntity<ChatbotSummaryResponseDto> getSummary(
            @Parameter(hidden = true, description = "인증된 사용자") @AuthenticationPrincipal User user,
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", example = "2025-04") @RequestParam("month") String month
    ) {
        ChatbotSummaryResponseDto result = chatbotService.getConsumptionSummary(user, month);
        return ResponseEntity.ok(result);
    }
}
