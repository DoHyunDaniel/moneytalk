// Polyfill for `global` in browser environments
if (typeof global === "undefined") {
  (window as any).global = window;
}

import SockJS from "sockjs-client";
import { Client, Stomp } from "@stomp/stompjs";

let stompClient: Client;

export const connectChatSocket = (
  roomId: number,
  onMessage: (msg: any) => void
) => {
  const socket = new SockJS("http://localhost:8080/ws-chat");
  stompClient = Stomp.over(socket);

  stompClient.onConnect = () => {
    stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg) => {
      const body = JSON.parse(msg.body);
      console.log("📥 메시지 수신:", body); // 이게 뜨면 성공!
      onMessage(body);
    });
  };
  stompClient.activate();
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
