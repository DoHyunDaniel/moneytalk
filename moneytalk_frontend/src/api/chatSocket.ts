import SockJS from "sockjs-client";
import { Client, IMessage, Stomp } from "@stomp/stompjs";

// Polyfill (브라우저 환경 대응)
if (typeof global === "undefined") {
  (window as any).global = window;
}

let stompClient: Client;

export interface SocketOptions {
  roomId: number;
  onMessage: (msg: any) => void;
  onConnectChange?: (status: "CONNECTED" | "RECONNECTING" | "DISCONNECTED") => void;
  onDisconnectForced?: () => void;
  onReconnectSuccess?: () => void; // ✅ NEW
}

export const connectChatSocket = ({
  roomId,
  onMessage,
  onConnectChange,
  onDisconnectForced,
  onReconnectSuccess,
}: SocketOptions): () => void => {
  const socket = new SockJS("http://localhost:8080/ws-chat");
  stompClient = Stomp.over(()=>socket);
  stompClient.debug = () => {};
  stompClient.reconnectDelay = 5000; // 자동 재연결 딜레이

  let wasDisconnected = false;
  let unsubscribeFn = () => {};

  stompClient.onConnect = () => {
    onConnectChange?.("CONNECTED");

    if (wasDisconnected && onReconnectSuccess) {
      onReconnectSuccess(); // ✅ 재연결 성공 시 알림
      wasDisconnected = false;
    }

    const subscription = stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg: IMessage) => {
      const body = JSON.parse(msg.body);
      onMessage(body);
    });

    unsubscribeFn = () => {
      subscription.unsubscribe();
      stompClient.deactivate();
      onConnectChange?.("DISCONNECTED");
    };
  };

  stompClient.onWebSocketClose = () => {
    onConnectChange?.("RECONNECTING");
    wasDisconnected = true;

    if (!stompClient.connected && onDisconnectForced) {
      onDisconnectForced();
    }
  };

  stompClient.activate();

  return () => {
    unsubscribeFn();
  };
};



export const sendChatMessage = (data: {
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  message: string;
  type: string;
  imageUrl?: string;
}) => {
  if (!stompClient || !stompClient.connected) return;
  stompClient.publish({
    destination: "/pub/chat/message",
    body: JSON.stringify(data),
  });
};

