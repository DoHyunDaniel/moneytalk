package com.example.moneytalk.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomSummaryDto {

    private Long chatRoomId;
    private String productTitle;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private String opponentNickname;
    private int unreadCount;
}
