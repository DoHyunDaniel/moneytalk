import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMyChatRooms, markMessagesAsRead } from "../api/chatApi";
import Skeleton from "react-loading-skeleton";
import "react-loading-skeleton/dist/skeleton.css";
import "../styles/ChatRoomList.css";
import { formatDistanceToNow } from "date-fns";

interface ChatRoomSummary {
  chatRoomId: number;
  productTitle: string;
  productThumbnailUrl: string;
  opponentNickname: string;
  opponentProfileImage: string;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
}

const ChatRoomList = () => {
  const [chatRooms, setChatRooms] = useState<ChatRoomSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const rooms = await fetchMyChatRooms();
        setChatRooms(rooms);
      } catch (error) {
        console.error("채팅방 불러오기 실패", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRooms();
    const interval = setInterval(fetchRooms, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleChatRoomClick = async (chatRoomId: number) => {
    try {
      await markMessagesAsRead(chatRoomId);
      navigate(`/chat/${chatRoomId}`);
    } catch (error) {
      console.error("채팅방 이동 또는 읽음 처리 실패", error);
    }
  };

  const ChatRoomSkeleton = () => (
    <li className="chat-room-item skeleton" style={{ display: "flex", gap: "12px", marginBottom: "16px" }}>
      <Skeleton width={80} height={80} />
      <div style={{ flex: 1 }}>
        <Skeleton width="60%" height={20} />
        <Skeleton width="40%" height={14} style={{ marginTop: 8 }} />
        <Skeleton width="80%" height={14} style={{ marginTop: 8 }} />
      </div>
    </li>
  );

  return (
    <div className="chat-room-list" style={{ maxWidth: "600px", margin: "0 auto", padding: "20px" }}>
      <h2>📂 나의 채팅방</h2>
      {isLoading ? (
        <ul style={{ padding: 0 }}>
          {Array.from({ length: 4 }).map((_, idx) => (
            <ChatRoomSkeleton key={idx} />
          ))}
        </ul>
      ) : chatRooms.length === 0 ? (
        <p>채팅방이 없습니다.</p>
      ) : (
        <ul style={{ padding: 0 }}>
          {chatRooms.map((room) => (
            <li
              key={room.chatRoomId}
              onClick={() => handleChatRoomClick(room.chatRoomId)}
              className="chat-room-item"
              style={{
                display: "flex",
                gap: "12px",
                marginBottom: "16px",
                cursor: "pointer",
                alignItems: "center"
              }}
            >
              <img
                src={room.productThumbnailUrl}
                alt="thumbnail"
                width={80}
                height={80}
                style={{ borderRadius: "8px", objectFit: "cover" }}
              />
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>
                  {room.productTitle}
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "8px", margin: "6px 0" }}>
                  <img
                    src={room.opponentProfileImage}
                    alt="상대 프로필"
                    width={24}
                    height={24}
                    style={{ borderRadius: "50%" }}
                  />
                  <span>{room.opponentNickname}</span>
                </div>
                <div style={{ fontSize: "13px", color: "#666" }}>
                  {room.lastMessage} · {formatDistanceToNow(new Date(room.lastMessageAt), { addSuffix: true })}
                </div>
                {room.unreadCount > 0 && (
                  <div style={{
                    marginTop: "4px",
                    backgroundColor: "red",
                    color: "white",
                    borderRadius: "12px",
                    padding: "2px 8px",
                    fontSize: "12px",
                    display: "inline-block",
                    fontWeight: 500
                  }}>
                    {room.unreadCount}
                  </div>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ChatRoomList;
