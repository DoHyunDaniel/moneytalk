import apiClient from "./axios";

/**
 * 특정 채팅방의 메시지 목록을 조회합니다.
 *
 * - GET /chatrooms/{chatRoomId}/messages
 * - 응답: ChatMessageDto[] (메시지 목록)
 *
 * @param chatRoomId 조회할 채팅방 ID
 * @returns 메시지 DTO 배열
 */
export const fetchChatMessages = async (chatRoomId: number) => {
  const res = await apiClient.get(`/chatrooms/${chatRoomId}/messages`);
  return res.data; // ChatMessageDto[]
};

/**
 * 현재 로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.
 *
 * - GET /chatrooms
 * - 응답: ChatRoomSummaryDto[] (상대방 정보, 마지막 메시지 등 포함)
 *
 * @returns 채팅방 요약 정보 리스트
 */
export const fetchMyChatRooms = async () => {
  const response = await apiClient.get("/chatrooms");
  return response.data;
};

/**
 * 특정 채팅방의 메시지를 모두 읽음 처리합니다.
 *
 * - PATCH /chatrooms/{chatRoomId}/read
 * - 서버에서 현재 유저의 안 읽은 메시지를 읽음 처리함
 *
 * @param chatRoomId 대상 채팅방 ID
 */
export const markMessagesAsRead = async (chatRoomId: number) => {
  await apiClient.patch(`/chatrooms/${chatRoomId}/read`);
};

/**
 * 채팅방 생성 요청
 *
 * - POST /chatrooms/{productId}
 * - 상품 상세에서 "채팅하기"를 누르면 호출됨
 *
 * @param productId 채팅을 시작할 상품의 ID
 * @returns 생성된 채팅방 정보 (ChatRoomResponseDto)
 */
export const createChatRoom = async (productId: number) => {
  const response = await apiClient.post(`/chatrooms/${productId}`);
  return response.data; // ChatRoomResponseDto
};
