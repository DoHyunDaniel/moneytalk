package com.example.moneytalk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.moneytalk.dto.ChatMessageDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper; // ✅ 이제 주입 받음

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            if (message.getBody() == null || message.getBody().length == 0) {
                log.warn("RedisSubscriber - 빈 메시지 수신");
                return;
            }

            // Redis에서 온 메시지를 ChatMessageDto로 변환
            String json = new String(message.getBody());
            ChatMessageDto chatMessage = objectMapper.readValue(json, ChatMessageDto.class);

            // WebSocket 브로드캐스트
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getChatRoomId(), chatMessage);

            log.info("RedisSubscriber - roomId: {}, message: {}", chatMessage.getChatRoomId(), chatMessage.getMessage());

        } catch (Exception e) {
            log.error("RedisSubscriber - 메시지 처리 중 에러", e);
        }
    }
}
