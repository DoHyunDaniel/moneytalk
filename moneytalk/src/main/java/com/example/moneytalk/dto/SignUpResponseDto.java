package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 성공 시 반환되는 응답 DTO입니다.
 * 생성된 유저의 식별 정보와 프로필 정보 일부를 포함합니다.
 */
@Getter
@AllArgsConstructor
@Schema(description = "회원가입 응답 DTO")
public class SignUpResponseDto {

    @Schema(description = "생성된 사용자 ID", example = "42")
    private Long userId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 닉네임", example = "dohyunnn")
    private String nickname;
}
