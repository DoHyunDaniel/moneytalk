package com.example.moneytalk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatImageUploadResponseDto {
    private String imageUrl;

    public ChatImageUploadResponseDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
