package com.example.moneytalk.controller;

import org.springframework.data.redis.listener.ChannelTopic;
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
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ChatImageUploadResponseDto;
import com.example.moneytalk.dto.ChatMessageDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.ChatRoomRepository;
import com.example.moneytalk.service.ChatMessageService;
import com.example.moneytalk.service.RedisPublisher;
import com.example.moneytalk.type.ErrorCode;

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

	/**
	 * 채팅 메시지 수신 - Redis 발행 프론트가 /pub/chat/message로 전송하면 이 메소드가 호출됩니다.
	 *
	 * @param chatMessageDto 클라이언트가 전송한 채팅 메시지 정보
	 * @param message        WebSocket 세션 정보가 포함된 메시지
	 * @throws GlobalException 로그인 인증 실패 시 발생
	 *                         (ErrorCode.WEBSOCKET_AUTHENTICATION_FAILED)
	 */
	@MessageMapping("/chat/pub")
	public void publishMessage(ChatMessageDto chatMessageDto, Message<?> message) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
		User loginUser = (User) accessor.getSessionAttributes().get("user");

		if (loginUser == null) {
			throw new GlobalException(ErrorCode.WEBSOCKET_AUTHENTICATION_FAILED);
		}

		log.info("ChatMessageController - Message received: {}", chatMessageDto.getMessage());

		// 수정: 새로 복사해서 senderId 강제 세팅
		ChatMessageDto updatedMessage = ChatMessageDto.builder().chatRoomId(chatMessageDto.getChatRoomId())
				.senderId(loginUser.getId()) // 로그인 유저 ID를 강제 주입
				.senderNickname(chatMessageDto.getSenderNickname()).message(chatMessageDto.getMessage())
				.type(chatMessageDto.getType()).imageUrl(chatMessageDto.getImageUrl())
				.sentAt(chatMessageDto.getSentAt()).build();

		// DB 저장
		chatMessageService.saveMessage(updatedMessage);

		// Redis 발행
		ChannelTopic topic = new ChannelTopic("chatroom:" + updatedMessage.getChatRoomId());
		redisPublisher.publish(topic, updatedMessage);
	}

	/**
	 * 채팅방 내 이미지 업로드 API
	 *
	 * @param roomId 이미지를 업로드할 채팅방 ID
	 * @param file   업로드할 이미지 파일 (Multipart 형식)
	 * @return 업로드된 이미지의 URL을 담은 응답 객체
	 */
	@Operation(summary = "채팅 이미지 업로드", description = "채팅방 내에서 사용할 이미지를 업로드하고 URL을 반환합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "업로드 성공", content = @Content(schema = @Schema(implementation = ChatImageUploadResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content) })
	@PostMapping("/{roomId}/image")
	public ResponseEntity<ChatImageUploadResponseDto> uploadChatImage(
			@Parameter(description = "이미지를 업로드할 채팅방 ID", example = "1") @PathVariable("roomId") Long roomId,
			@Parameter(description = "업로드할 이미지 파일", required = true) @RequestPart("file") MultipartFile file) {
		String url = s3Uploader.uploadFile(file, "chat-images");

		ChatImageUploadResponseDto responseDto = ChatImageUploadResponseDto.builder().imageUrl(url).build();

		return ResponseEntity.ok(responseDto);
	}

}