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
  const socket = new SockJS("http://localhost:8080/ws-stomp");

  let wasDisconnected = false;
  let unsubscribeFn = () => {};

  stompClient = new Client({
    webSocketFactory: () => socket,
    debug: (msg) => console.log("[STOMP DEBUG]", msg),
    reconnectDelay: 5000,
    onConnect: () => {
      console.log("✅ STOMP CONNECTED!");

      const subscription = stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg: IMessage) => {
        const body = JSON.parse(msg.body);
        console.log("✅ 수신된 메시지 본문:", body); 
        onMessage(body);
      });

      unsubscribeFn = () => {
        subscription.unsubscribe();
        stompClient.deactivate();
        onConnectChange?.("DISCONNECTED");
      };

      onConnectChange?.("CONNECTED");

      if (wasDisconnected && onReconnectSuccess) {
        onReconnectSuccess();
        wasDisconnected = false;
      }
    },
    onStompError: (frame) => {
      console.error("❗ STOMP Error: ", frame);
    },
    onWebSocketClose: () => {
      console.warn("❗ WebSocket Close");
      onConnectChange?.("RECONNECTING");
      wasDisconnected = true;
      if (!stompClient.connected && onDisconnectForced) {
        onDisconnectForced();
      }
    }
  });

  stompClient.activate(); // ✅ 여기 한 번만

  return () => {
    unsubscribeFn();
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
    destination: "/pub/chat/pub",
    body: JSON.stringify(data),
  });
};
