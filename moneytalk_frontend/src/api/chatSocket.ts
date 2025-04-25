import SockJS from "sockjs-client";
import { Client, IMessage, Stomp } from "@stomp/stompjs";

// ✅ 브라우저 환경 대응: 글로벌 오브젝트 정의 (SockJS 내부 의존성 대응용)
if (typeof global === "undefined") {
  (window as any).global = window;
}

let stompClient: Client; // STOMP 클라이언트 전역 관리

/**
 * WebSocket 연결 설정 옵션
 */
export interface SocketOptions {
  roomId: number;
  onMessage: (msg: any) => void; // 서버로부터 수신된 메시지 콜백
  onConnectChange?: (status: "CONNECTED" | "RECONNECTING" | "DISCONNECTED") => void; // 연결 상태 변경 콜백
  onDisconnectForced?: () => void; // 강제 종료(수동 해제 등) 콜백
  onReconnectSuccess?: () => void; // 자동 재연결 성공 시 콜백
}

/**
 * STOMP 기반 채팅 소켓 연결 함수
 *
 * - SockJS를 통해 백엔드 WebSocket에 연결
 * - 연결 성공 시 `/sub/chat/room/{roomId}`에 subscribe
 * - 자동 재연결 및 상태 변화에 대한 콜백 지원
 *
 * @param options SocketOptions
 * @returns unsubscribe 함수 (연결 해제용)
 */
export const connectChatSocket = ({
  roomId,
  onMessage,
  onConnectChange,
  onDisconnectForced,
  onReconnectSuccess,
}: SocketOptions): () => void => {
  const socket = new SockJS("http://localhost:8080/ws-chat");
  stompClient = Stomp.over(() => socket);
  stompClient.debug = () => {}; // 디버깅 로그 비활성화
  stompClient.reconnectDelay = 5000; // 5초 후 자동 재연결

  let wasDisconnected = false;
  let unsubscribeFn = () => {};

  // 연결 성공 시
  stompClient.onConnect = () => {
    onConnectChange?.("CONNECTED");

    // 재연결 후 콜백 트리거
    if (wasDisconnected && onReconnectSuccess) {
      onReconnectSuccess();
      wasDisconnected = false;
    }

    // 채팅방 구독 시작
    const subscription = stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg: IMessage) => {
      const body = JSON.parse(msg.body);
      onMessage(body); // 수신 메시지 핸들링
    });

    // 언서브 함수 정의
    unsubscribeFn = () => {
      subscription.unsubscribe();
      stompClient.deactivate();
      onConnectChange?.("DISCONNECTED");
    };
  };

  // 소켓 연결이 끊겼을 때
  stompClient.onWebSocketClose = () => {
    onConnectChange?.("RECONNECTING");
    wasDisconnected = true;

    if (!stompClient.connected && onDisconnectForced) {
      onDisconnectForced();
    }
  };

  stompClient.activate(); // 연결 시작

  return () => {
    unsubscribeFn(); // 클린업 함수 반환
  };
};

/**
 * 채팅 메시지 전송 함수
 *
 * - /pub/chat/message 엔드포인트로 메시지를 publish
 * - stompClient 연결 여부 확인 후 전송
 *
 * @param data 메시지 데이터
 */
export const sendChatMessage = (data: {
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  message: string;
  type: string; // 예: TEXT, IMAGE, SYSTEM
  imageUrl?: string;
}) => {
  if (!stompClient || !stompClient.connected) return;

  stompClient.publish({
    destination: "/pub/chat/message",
    body: JSON.stringify(data),
  });
};
