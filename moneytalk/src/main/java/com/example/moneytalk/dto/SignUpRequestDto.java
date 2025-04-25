package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 요청 DTO입니다.
 * 이메일, 비밀번호, 닉네임을 입력받아 신규 사용자 계정을 생성합니다.
 */
@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequestDto {

    @Schema(description = "이메일 주소 (로그인 ID로 사용)", example = "user@example.com", required = true)
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @Schema(description = "비밀번호", example = "securePassword123!", required = true)
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @Schema(description = "닉네임", example = "dohyunnn", required = true)
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
}
