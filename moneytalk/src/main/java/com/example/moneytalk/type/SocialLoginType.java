package com.example.moneytalk.type;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소셜 로그인 플랫폼 유형을 나타내는 열거형(Enum)입니다.
 * - GOOGLE: 구글 로그인
 * - KAKAO: 카카오 로그인
 * - NAVER: 네이버 로그인
 */
@Schema(description = "소셜 로그인 유형")
public enum SocialLoginType {

    @Schema(description = "구글 로그인")
    GOOGLE,

    @Schema(description = "카카오 로그인")
    KAKAO,

    @Schema(description = "네이버 로그인")
    NAVER
}
