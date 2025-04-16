package com.example.moneytalk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNicknameRequestDto {
    @NotBlank
    private String nickname;
}
