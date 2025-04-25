package com.example.moneytalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 채팅 이미지 업로드 후 반환되는 이미지 URL 응답 DTO입니다.
 */
@Getter
@Setter
@Schema(description = "채팅 이미지 업로드 응답 DTO")
public class ChatImageUploadResponseDto {

    @Schema(
        description = "업로드된 이미지의 S3 URL",
        example = "https://moneytalk-s3.s3.ap-northeast-2.amazonaws.com/chat-images/sample.jpg",
        required = true
    )
    private String imageUrl;

    public ChatImageUploadResponseDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
