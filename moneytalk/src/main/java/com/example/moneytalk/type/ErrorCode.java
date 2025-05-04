package com.example.moneytalk.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	USER_NOT_FOUND(404, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
	SENDER_NOT_FOUND(404, "SENDER_NOT_FOUND","메세지 송신자가 존재하지 않습니다."),
	RECEIVER_NOT_FOUND(404, "RECEIVER_NOT_FOUND","메세지 수신자가 존재하지 않습니다."),
	PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
	INVALID_INPUT_VALUE(400, "INVALID_INPUT_VALUE", "올바르지 않은 입력입니다."),
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),
	PRODUCT_ALREADY_SOLD(404,"PRODUCT_ALREADY_SOLD", "이미 판매완료된 상품입니다."),
	PRODUCT_ACCESS_DENIED(403, "PRODUCT_ACCESS_DENIED","상품 수정 권한이 없습니다."),
	CANNOT_PURCHASE_OWN_PRODUCT(400, "CANNOT_PURCHASE_OWN_PRODUCT", "본인의 상품은 구매확정할 수 없습니다."),
	EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(409, "NICKNAME_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다."),
	INVALID_NICKNAME_FORMAT(400, "INVALID_NICKNAME_FORMAT", "허용되지 않는 닉네임 형식입니다."),
	EMAIL_NOT_FOUND(404, "EMAIL_NOT_FOUND", "존재하지 않는 이메일입니다."),
	INVALID_PASSWORD(401, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
	
	// 리뷰 관련 에러
	REVIEW_NOT_FOUND(404, "REVIEW_NOT_FOUND", "리뷰가 존재하지 않습니다."),
	PRODUCT_NOT_SOLD_YET(404, "PRODUCT_NOT_SOLD_YET", "아직 구매가 확정되지 않은 상품입니다."),
	REVIEW_WRITE_FORBIDDEN(403, "REVIEW_WRITE_FORBIDDEN", "해당 상품을 구매한 사용자만 리뷰를 작성할 수 있습니다."),
	CANNOT_REVIEW_SELF(400, "CANNOT_REVIEW_SELF", "자기 자신에게는 리뷰를 작성할 수 없습니다."),
	REVIEW_ALREADY_WRITTEN(409, "REVIEW_ALREADY_WRITTEN", "이미 리뷰를 작성한 상품입니다."),
	REVIEW_UPDATE_FORBIDDEN(403, "REVIEW_UPDATE_FORBIDDEN", "본인의 리뷰만 수정할 수 있습니다."),
	REVIEW_DELETE_FORBIDDEN(403, "REVIEW_DELETE_FORBIDDEN", "본인의 리뷰만 삭제할 수 있습니다."),

	// 채팅 기능 에러
	WEBSOCKET_AUTHENTICATION_FAILED(401, "WEBSOCKET_AUTHENTICATION_FAILED", "WebSocket 인증 실패: 유저 정보 없음"),
	CHATROOM_NOT_FOUND(404, "CHATROOM_NOT_FOUND", "채팅방이 존재하지 않습니다."),
	CHATROOM_ACCESS_DENIED(403, "CHATROOM_ACCESS_DENIED", "해당 채팅방에 접근할 수 없습니다."),

	// 예산, 구매이력 관련 에러
	BUDGET_NOT_FOUND(404, "BUDGET_NOT_FOUND", "예산 정보가 없습니다.")
	;

	private final Integer status;
	private final String error;
	private final String message;
}
