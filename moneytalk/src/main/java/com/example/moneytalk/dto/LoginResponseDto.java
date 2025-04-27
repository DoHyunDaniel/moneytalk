package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 성공 시 반환되는 응답 DTO입니다.
 * JWT 토큰, 이메일, 닉네임 정보를 포함합니다.
 */
@Getter    
@Builder
@Schema(description = "로그인 응답 DTO")
public class LoginResponseDto {

    @Schema(description = "JWT 인증 토큰 (Bearer Token)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String token;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private final String email;

    @Schema(description = "사용자 닉네임", example = "dohyunnn")
    private final String nickname;
}
