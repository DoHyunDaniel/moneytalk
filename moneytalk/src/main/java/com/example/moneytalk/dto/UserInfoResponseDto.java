package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인된 사용자 또는 특정 유저의 기본 정보 응답 DTO입니다.
 */
@Getter
@Builder
@Schema(description = "사용자 정보 응답 DTO")
public class UserInfoResponseDto {

    @Schema(description = "사용자 ID", example = "42")
    private final Long userId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private final String email;

    @Schema(description = "사용자 닉네임", example = "dohyunnn")
    private final String nickname;
}
