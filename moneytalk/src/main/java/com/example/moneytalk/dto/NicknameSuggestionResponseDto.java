package com.example.moneytalk.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 닉네임 중복 확인 및 추천 응답 DTO입니다.
 * 기본 닉네임의 사용 가능 여부와 추천 대안을 제공합니다.
 */
@Getter
@Builder
@Schema(description = "닉네임 중복 확인 및 추천 응답 DTO")
public class NicknameSuggestionResponseDto {

    @Schema(description = "사용자가 입력한 닉네임", example = "dohyunnn", required = true)
    private final String base;

    @Schema(description = "입력한 닉네임 사용 가능 여부", example = "false", required = true)
    private final boolean available;

    @Schema(description = "대체 추천 닉네임 리스트", example = "[\"dohyunnn1\", \"dohyunnn_99\", \"dohyunnn_dev\"]")
    private final List<String> suggestions;
}
