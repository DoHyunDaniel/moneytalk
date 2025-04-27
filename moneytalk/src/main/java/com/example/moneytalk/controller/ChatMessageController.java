package com.example.moneytalk.controller;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.example.moneytalk.service.RedisPublisher;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final RedisPublisher redisPublisher;

    // WebSocket기반 채팅 구현용 선언
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelTopic channelTopic;

    /**
     * 채팅 메시지 수신 - Redis 발행
     * 프론트가 /pub/chat/message 로 전송하면 이 메소드가 호출됩니다.
     */
    @MessageMapping("/chat/pub")
    public void publishMessage(ChatMessageDto chatMessageDto, Message<?> message) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        User loginUser = (User) accessor.getSessionAttributes().get("user");

        if (loginUser == null) {
            throw new IllegalStateException("WebSocket 인증 실패: 유저 정보 없음");
        }

        log.info("ChatMessageController - Message received: {}", chatMessageDto.getMessage());

        // 수정: 새로 복사해서 senderId 강제 세팅
        ChatMessageDto updatedMessage = ChatMessageDto.builder()
                .chatRoomId(chatMessageDto.getChatRoomId())
                .senderId(loginUser.getId()) // 로그인 유저 ID를 강제 주입
                .senderNickname(chatMessageDto.getSenderNickname())
                .message(chatMessageDto.getMessage())
                .type(chatMessageDto.getType())
                .imageUrl(chatMessageDto.getImageUrl())
                .sentAt(chatMessageDto.getSentAt())
                .build();

        // DB 저장
        chatMessageService.saveMessage(updatedMessage);

        // Redis 발행
        ChannelTopic topic = new ChannelTopic("chatroom:" + updatedMessage.getChatRoomId());
        redisPublisher.publish(topic, updatedMessage);
    }



    /**
     * 채팅방 내 이미지 업로드 API
     */
    @Operation(summary = "채팅 이미지 업로드", description = "채팅방 내에서 사용할 이미지를 업로드하고 URL을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업로드 성공",
            content = @Content(schema = @Schema(implementation = ChatImageUploadResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/{roomId}/image")
    public ResponseEntity<ChatImageUploadResponseDto> uploadChatImage(
        @Parameter(description = "이미지를 업로드할 채팅방 ID", example = "1")
        @PathVariable("roomId") Long roomId,
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestPart("file") MultipartFile file
    ) {
        String url = s3Uploader.uploadFile(file, "chat-images");

        ChatImageUploadResponseDto responseDto = ChatImageUploadResponseDto.builder()
            .imageUrl(url)
            .build();

        return ResponseEntity.ok(responseDto);
    }

    
//  @MessageMapping("/chat/pub")
//  public void publishMessage(@AuthenticationPrincipal(expression = "id") Long senderId,
//                              ChatMessageDto chatMessageDto) {
//
//      log.info("ChatMessageController - Message received: {}", chatMessageDto.getMessage());
//
//      // 보안을 위해 senderId를 강제 설정
//      chatMessageDto.setSenderId(senderId);
//
//      // DB에 메시지 저장
//      chatMessageService.saveMessage(chatMessageDto);
//
//      // Redis에 발행
//      ChannelTopic topic = new ChannelTopic("chatroom:" + chatMessageDto.getChatRoomId());
//      redisPublisher.publish(topic, chatMessageDto);
//  }
    
//    /**
//     * WebSocket을 통해 전송된 채팅 메시지를 처리합니다.
//     * 1. 메시지를 저장하고,
//     * 2. 수신자를 판별한 후,
//     * 3. 해당 채팅방 구독자에게 메시지를 브로드캐스트합니다.
//     *
//     * @param messageDto 클라이언트로부터 수신된 채팅 메시지 DTO
//     * @param message WebSocket 메시지 객체 (세션 속성 포함)
//     * @throws IllegalStateException 인증되지 않은 사용자일 경우
//     * @throws IllegalArgumentException 채팅방이 존재하지 않을 경우
//     */
//    @MessageMapping("/chat/message/direct")
//    public void handleMessage(ChatMessageDto messageDto, Message<?> message) {
//        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
//        User loginUser = (User) accessor.getSessionAttributes().get("user");
//
//        if (loginUser == null) {
//            throw new IllegalStateException("WebSocket 인증 실패: 유저 정보 없음");
//        }
//
//        messageDto.setSenderId(loginUser.getId());
//
//        // 1. 메시지 저장
//        ChatMessageDto saved = chatMessageService.saveMessage(messageDto);
//
//        // 2. 수신자 계산
//        ChatRoom room = chatRoomRepository.findById(messageDto.getChatRoomId())
//            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
//
//        Long receiverId = (room.getBuyer().getId().equals(loginUser.getId()))
//            ? room.getSeller().getId()
//            : room.getBuyer().getId();
//
//        // 3. 구독 확인 후 메시지 전송
//        if (chatMessageService.isUserSubscribedToRoom(room.getId(), receiverId)) {
//            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getId(), saved);
//        }
//    }
//
//    /**
//     * 채팅방 내 이미지 업로드 API입니다.
//     * S3에 이미지를 업로드하고, 업로드된 이미지 URL을 반환합니다.
//     *
//     * @param roomId 이미지가 첨부될 채팅방 ID
//     * @param file 업로드할 이미지 파일
//     * @return 업로드된 이미지의 URL
//     */
//    @Operation(
//        summary = "채팅 이미지 업로드",
//        description = "채팅방 내에서 사용할 이미지를 업로드하고, 업로드된 이미지의 URL을 반환합니다."
//    )
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "업로드 성공",
//            content = @Content(schema = @Schema(implementation = ChatImageUploadResponseDto.class))),
//        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
//        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
//    })
//    @PostMapping("/{roomId}/image")
//    public ResponseEntity<ChatImageUploadResponseDto> uploadChatImage(
//        @Parameter(description = "이미지를 업로드할 채팅방 ID", example = "1")
//        @PathVariable("roomId") Long roomId,
//        @Parameter(description = "업로드할 이미지 파일", required = true)
//        @RequestPart("file") MultipartFile file
//    ) {
//        String url = s3Uploader.uploadFile(file, "chat-images");
//        return ResponseEntity.ok(new ChatImageUploadResponseDto(url));
//    }
//    
//
//    @MessageMapping("/chat/message/pub")
//    public void message(@AuthenticationPrincipal(expression = "id") Long senderId,
//                        ChatMessageDto chatMessageDto) {
//
//        log.info("WebSocketController - Message received: {}", chatMessageDto.getMessage());
//
//        // senderId를 강제 설정 (보안)
//        chatMessageDto.setSenderId(senderId);
//
//        // 보낼 채널을 동적으로 지정 (ex: chatroom:{chatRoomId})
//        ChannelTopic topic = new ChannelTopic("chatroom:" + chatMessageDto.getChatRoomId());
//
//        // Redis를 통해 메시지 발행
//        redisPublisher.publish(topic, chatMessageDto);
//    }
}
