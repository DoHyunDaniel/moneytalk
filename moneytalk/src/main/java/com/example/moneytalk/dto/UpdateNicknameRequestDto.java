package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 닉네임 변경 요청 DTO입니다.
 * 새로운 닉네임을 입력받아 유저 정보를 갱신합니다.
 */
@Getter
@NoArgsConstructor(force = true)
@Schema(description = "닉네임 변경 요청 DTO")
public class UpdateNicknameRequestDto {

    @Schema(description = "변경할 닉네임", example = "dohyunnn_new", required = true)
    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @Builder
    public UpdateNicknameRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
