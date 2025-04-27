package com.example.moneytalk.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatbotSummaryResponseDto;
import com.example.moneytalk.dto.LedgerMonthlySummaryResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * OpenAI API를 이용하여 월간 소비 내역을 자연어로 요약해주는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final LedgerService ledgerService;
    private final RestTemplate restTemplate = new RestTemplate(); // 향후 WebClient로 대체 가능

    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * 사용자의 특정 월 소비 데이터를 기반으로 GPT 모델에게 소비 요약을 요청합니다.
     *
     * @param user  사용자 정보
     * @param month 조회할 월 (yyyy-MM)
     * @return AI가 분석한 소비 요약 결과
     */
    public ChatbotSummaryResponseDto getConsumptionSummary(User user, String month) {
        LedgerMonthlySummaryResponseDto summary = ledgerService.getMonthlySummary(user, month);
        String prompt = buildPrompt(summary);

        // OpenAI API 호출 준비
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
        {
          "model": "gpt-3.5-turbo",
          "messages": [
            {"role": "system", "content": "너는 가계부 분석 전문가야."},
            {"role": "user", "content": "%s"}
          ]
        }
        """.formatted(prompt);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
        String result = extractContent(response);

        return ChatbotSummaryResponseDto.builder()
                .summary(result)
                .build();
    }

    /**
     * 소비 요약용 프롬프트를 생성합니다.
     *
     * @param summary LedgerMonthlySummaryResponseDto 객체
     * @return ChatGPT에 전달할 자연어 프롬프트 문자열
     */
    private String buildPrompt(LedgerMonthlySummaryResponseDto summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("이번 달 예산은 ").append(summary.getBudget()).append("원이고, ");
        sb.append("총 지출은 ").append(summary.getTotalExpense()).append("원입니다. ");

        if (summary.getCategorySummary() != null && !summary.getCategorySummary().isEmpty()) {
            sb.append("카테고리별 지출은 다음과 같습니다.\n");
            for (Map.Entry<String, Integer> entry : summary.getCategorySummary().entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append("원\n");
            }
        }

        sb.append("이 데이터를 바탕으로 소비 습관을 분석해서 요약해줘.");
        return sb.toString();
    }

    /**
     * OpenAI 응답에서 요약 문자열(content)만 추출합니다.
     *
     * @param response OpenAI API 응답
     * @return 요약 텍스트 또는 오류 메시지
     */
    private String extractContent(ResponseEntity<Map> response) {
        try {
            Map<String, Object> body = response.getBody();
            var choices = (List<Map<String, Object>>) body.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "요약 결과를 가져오는 데 실패했습니다.";
        }
    }
}
