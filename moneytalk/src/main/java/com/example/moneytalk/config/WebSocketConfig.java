package com.example.moneytalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 endpoint
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // CORS
                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 경로: ex) /sub/chat/room/1
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행하는 경로: ex) /pub/chat/message
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
