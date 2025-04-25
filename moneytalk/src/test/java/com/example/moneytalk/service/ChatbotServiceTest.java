package com.example.moneytalk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatbotSummaryResponseDto;
import com.example.moneytalk.dto.LedgerMonthlySummaryResponseDto;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatbotService = new ChatbotService(ledgerService);
        ReflectionTestUtils.setField(chatbotService, "apiKey", "test-key"); // ✅ apiKey 주입
        ReflectionTestUtils.setField(chatbotService, "restTemplate", restTemplate); // ✅ RestTemplate도 교체
    }

    @Test
    void 소비_요약_정상_동작() {
        // given
        User user = User.builder().id(1L).email("test@example.com").build();

        LedgerMonthlySummaryResponseDto ledgerSummary = LedgerMonthlySummaryResponseDto.builder()
                .totalIncome(3000000)
                .totalExpense(600000)
                .budget(500000)
                .isExceeded(true)
                .categorySummary(Map.of("식비", 250000, "쇼핑", 200000))
                .build();

        when(ledgerService.getMonthlySummary(user, "2025-04")).thenReturn(ledgerSummary);

        // GPT 응답 Mock 구성
        Map<String, Object> message = Map.of("content", "요약 결과입니다: 식비가 많아요.");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(body, HttpStatus.OK);

        // restTemplate.exchange() 결과를 Mock
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // when
        ChatbotSummaryResponseDto result = chatbotService.getConsumptionSummary(user, "2025-04");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).contains("식비가 많아요");
    }
}
