package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 시 사용되는 DTO입니다.
 * 이메일과 비밀번호를 입력받습니다.
 */
@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {

    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "securePassword123!", required = true)
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
    
}
