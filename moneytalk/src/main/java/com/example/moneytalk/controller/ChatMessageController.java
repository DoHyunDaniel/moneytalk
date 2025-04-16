package com.example.moneytalk.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message")
    public void handleMessage(ChatMessageDto messageDto) {
        try {
            System.out.println("📨 받은 메시지 DTO: " + messageDto.getMessage());

            ChatMessageDto saved = chatMessageService.saveMessage(messageDto);

            messagingTemplate.convertAndSend(
                "/sub/chat/room/" + messageDto.getChatRoomId(),
                saved
            );

        } catch (Exception e) {
            System.out.println("❌ 채팅 메시지 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
