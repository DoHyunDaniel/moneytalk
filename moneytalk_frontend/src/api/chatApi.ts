import apiClient from "./axios";

// 채팅 메시지 조회
export const fetchChatMessages = async (chatRoomId: number) => {
  const res = await apiClient.get(`/chatrooms/${chatRoomId}/messages`);
  return res.data; // ChatMessageDto[]
};

// 내 채팅방 목록 조회
export const fetchMyChatRooms = async () => {
  const response = await apiClient.get("/chatrooms");
  return response.data;
};

// 채팅방의 메시지를 모두 읽음 처리
export const markMessagesAsRead = async (chatRoomId: number) => {
  await apiClient.patch(`/chatrooms/${chatRoomId}/read`);
};
