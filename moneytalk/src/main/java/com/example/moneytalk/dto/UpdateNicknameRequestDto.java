package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 닉네임 변경 요청 DTO입니다.
 * 새로운 닉네임을 입력받아 유저 정보를 갱신합니다.
 */
@Getter
@Setter
@Schema(description = "닉네임 변경 요청 DTO")
public class UpdateNicknameRequestDto {

    @Schema(description = "변경할 닉네임", example = "dohyunnn_new", required = true)
    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    private String nickname;
}
