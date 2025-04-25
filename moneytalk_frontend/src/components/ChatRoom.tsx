import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { connectChatSocket, sendChatMessage } from "../api/chatSocket";
import { fetchChatMessages, markMessagesAsRead } from "../api/chatApi";
import { logout, getMyInfo } from "../api/auth";
import { format } from "date-fns";

/**
 * 채팅 메시지 객체 구조
 */
interface ChatMessage {
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  message: string;
  type: string;
  imageUrl?: string;
  sentAt: string;
}

/**
 * 실시간 채팅방 컴포넌트
 *
 * - 로그인 유저 ID 확인 → 메시지 불러오기 → 소켓 연결
 * - 메시지 수신 시 갱신 + 읽음 처리 + 스크롤 이동
 * - 연결 상태 및 강제 로그아웃, 자동 재연결 대응
 */
const ChatRoom = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [connectionStatus, setConnectionStatus] = useState<
    "CONNECTED" | "DISCONNECTED" | "RECONNECTING"
  >("DISCONNECTED");
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  const scrollRef = useRef<HTMLDivElement>(null);

  /**
   * 메시지 영역 맨 아래로 스크롤
   */
  const scrollToBottom = () => {
    setTimeout(() => {
      scrollRef.current?.scrollIntoView({ behavior: "smooth" });
    }, 30);
  };

  /**
   * 1. 로그인된 사용자 정보 불러오기
   */
  useEffect(() => {
    getMyInfo()
      .then((res) => {
        setCurrentUserId(res.userId);
      })
      .catch(() => alert("로그인 정보 확인 실패"));
  }, []);

  /**
   * 2. 채팅방 메시지 조회 + 소켓 연결 + 읽음 처리
   */
  useEffect(() => {
    if (!roomId || currentUserId === null) return;

    // 메시지 불러오기
    fetchChatMessages(Number(roomId)).then((data) => {
      setMessages(data);
      markMessagesAsRead(Number(roomId)).catch(() => {
        console.warn("읽음 처리 실패");
      });
      scrollToBottom();
    });

    // 소켓 연결 및 콜백 설정
    const unsubscribe = connectChatSocket({
      roomId: Number(roomId),
      onMessage: (msg) => {
        setMessages((prev) => [...prev, msg]);
        scrollToBottom();
      },
      onConnectChange: setConnectionStatus,
      onDisconnectForced: async () => {
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        await logout();
        navigate("/login");
      },
      onReconnectSuccess: () => {
        alert("소켓 연결이 복구되었습니다!");
      },
    });

    return () => unsubscribe(); // cleanup
  }, [roomId, currentUserId]);

  /**
   * 메시지 전송 핸들러
   */
  const handleSend = () => {
    if (
      !input.trim() ||
      !roomId ||
      connectionStatus !== "CONNECTED" ||
      currentUserId === null
    ) {
      alert("메시지를 보내려면 먼저 서버와 연결되어야 합니다.");
      return;
    }

    sendChatMessage({
      chatRoomId: Number(roomId),
      senderId: currentUserId,
      senderNickname: "",
      message: input,
      type: "TEXT",
    });
    setInput("");
  };

  // 아직 유저 정보 불러오는 중
  if (currentUserId === null) {
    return (
      <div style={{ padding: "20px", textAlign: "center" }}>Loading...</div>
    );
  }

  return (
    <div style={{ maxWidth: "600px", margin: "0 auto", padding: "20px" }}>
      <h2>💬 채팅방 #{roomId}</h2>

      <div style={{ marginBottom: "8px", fontSize: "14px", color: "#555" }}>
        연결 상태: <b>{connectionStatus}</b>
      </div>

      {/* 메시지 영역 */}
      <div
        style={{
          maxHeight: "400px",
          overflowY: "auto",
          padding: "12px",
          border: "1px solid #ddd",
          borderRadius: "8px",
          marginBottom: "12px",
          background: "#fafafa",
        }}
      >
        {messages.map((msg, idx) => {
          const isMine = Number(msg.senderId) === Number(currentUserId);

          return (
            <div
              key={idx}
              style={{
                display: "flex",
                justifyContent: isMine ? "flex-end" : "flex-start",
                marginBottom: "12px",
              }}
            >
              <div
                style={{
                  background: isMine ? "#dcf8c6" : "#fff",
                  padding: "8px 12px",
                  borderRadius: "16px",
                  maxWidth: "75%",
                  boxShadow: "0 1px 2px rgba(0,0,0,0.1)",
                  textAlign: "left",
                }}
              >
                <div style={{ fontSize: "14px" }}>{msg.message}</div>
                <div
                  style={{
                    fontSize: "12px",
                    color: "#888",
                    marginTop: "4px",
                  }}
                >
                  {format(new Date(msg.sentAt), "yy.MM.dd HH:mm")}
                </div>
              </div>
            </div>
          );
        })}

        <div ref={scrollRef} />
      </div>

      {/* 입력창 */}
      <div style={{ display: "flex", gap: "8px" }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="메시지 입력"
          style={{
            flex: 1,
            padding: "8px",
            borderRadius: "8px",
            border: "1px solid #ccc",
          }}
        />
        <button
          onClick={handleSend}
          style={{
            padding: "8px 16px",
            background: "#007bff",
            color: "#fff",
            borderRadius: "8px",
            border: "none",
          }}
        >
          보내기
        </button>
      </div>
    </div>
  );
};

export default ChatRoom;
