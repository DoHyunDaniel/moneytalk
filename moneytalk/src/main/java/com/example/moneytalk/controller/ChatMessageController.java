package com.example.moneytalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatImageUploadResponseDto;
import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomRepository chatRoomRepository;
    private final S3Uploader s3Uploader;
    
    @MessageMapping("/chat/message")
    public void handleMessage(ChatMessageDto messageDto, Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        User loginUser = (User) accessor.getSessionAttributes().get("user");

        if (loginUser == null) {
            throw new IllegalStateException("WebSocket 인증 실패: 유저 정보 없음");
        }

        messageDto.setSenderId(loginUser.getId());

        // 1. 메시지 저장
        ChatMessageDto saved = chatMessageService.saveMessage(messageDto);

        // 2. 수신자 계산
        ChatRoom room = chatRoomRepository.findById(messageDto.getChatRoomId())
            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        Long receiverId = (room.getBuyer().getId().equals(loginUser.getId()))
            ? room.getSeller().getId()
            : room.getBuyer().getId();

        // 3. 구독 확인 후 메시지 전송
        if (chatMessageService.isUserSubscribedToRoom(room.getId(), receiverId)) {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getId(), saved);
        }
    }

    
    @PostMapping("/{roomId}/image")
    public ResponseEntity<ChatImageUploadResponseDto> uploadChatImage(
        @PathVariable Long roomId,
        @RequestPart("file") MultipartFile file
    ) {
        String url = s3Uploader.uploadFile(file, "chat-images");
        return ResponseEntity.ok(new ChatImageUploadResponseDto(url));
    }

    
    

}
