package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 소비 요약 챗봇의 응답을 나타내는 DTO입니다.
 * 지정된 월의 소비 데이터를 요약한 자연어 응답을 포함합니다.
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "소비 요약 챗봇 응답 DTO")
public class ChatbotSummaryResponseDto {

    @Schema(description = "월간 소비에 대한 자연어 요약", example = "4월에는 식비 비중이 가장 높았고, 쇼핑이 두 번째로 많았습니다.")
    private String summary;
}
