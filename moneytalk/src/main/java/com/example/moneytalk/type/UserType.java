package com.example.moneytalk.type;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 유형을 나타내는 열거형(Enum)입니다.
 * - USER: 일반 사용자
 * - ADMIN: 관리자
 */
@Schema(description = "사용자 유형")
public enum UserType {

    @Schema(description = "일반 사용자")
    USER,

    @Schema(description = "관리자 사용자 (운영 기능 접근 가능)")
    ADMIN
}
