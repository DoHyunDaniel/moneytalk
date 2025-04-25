import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchMyChatRooms, markMessagesAsRead } from "../api/chatApi";
import Skeleton from "react-loading-skeleton";
import "react-loading-skeleton/dist/skeleton.css";
import "../styles/ChatRoomList.css";
import { formatDistanceToNow } from "date-fns";

/**
 * 채팅방 요약 정보 인터페이스
 */
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

/**
 * 나의 채팅방 목록 컴포넌트
 *
 * - 서버에서 참여 중인 채팅방 목록을 주기적으로 불러옵니다.
 * - 각 항목 클릭 시 메시지 읽음 처리 후 상세 페이지로 이동합니다.
 * - 로딩 중에는 Skeleton UI를 표시합니다.
 */
const ChatRoomList = () => {
  const [chatRooms, setChatRooms] = useState<ChatRoomSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  /**
   * 채팅방 목록 불러오기 (초기 & 5초 주기)
   */
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

    fetchRooms(); // 최초 실행
    const interval = setInterval(fetchRooms, 5000); // 5초 간격 갱신

    return () => clearInterval(interval); // 언마운트 시 인터벌 제거
  }, []);

  /**
   * 채팅방 클릭 → 읽음 처리 후 상세 페이지로 이동
   */
  const handleChatRoomClick = async (chatRoomId: number) => {
    try {
      await markMessagesAsRead(chatRoomId);
      navigate(`/chat/${chatRoomId}`);
    } catch (error) {
      console.error("채팅방 이동 또는 읽음 처리 실패", error);
    }
  };

  /**
   * 채팅방 로딩 중 표시되는 Skeleton 컴포넌트
   */
  const ChatRoomSkeleton = () => (
    <li
      className="chat-room-item skeleton"
      style={{ display: "flex", gap: "12px", marginBottom: "16px" }}
    >
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
        // ✅ 로딩 중: Skeleton 4개 표시
        <ul style={{ padding: 0 }}>
          {Array.from({ length: 4 }).map((_, idx) => (
            <ChatRoomSkeleton key={idx} />
          ))}
        </ul>
      ) : chatRooms.length === 0 ? (
        // ✅ 채팅방 없음
        <p>채팅방이 없습니다.</p>
      ) : (
        // ✅ 채팅방 목록 렌더링
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
                alignItems: "center",
              }}
            >
              {/* 상품 썸네일 */}
              <img
                src={room.productThumbnailUrl}
                alt="thumbnail"
                width={80}
                height={80}
                style={{ borderRadius: "8px", objectFit: "cover" }}
              />

              {/* 채팅방 정보 */}
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>
                  {room.productTitle}
                </div>

                {/* 상대방 프로필 */}
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px",
                    margin: "6px 0",
                  }}
                >
                  <img
                    src={room.opponentProfileImage}
                    alt="상대 프로필"
                    width={24}
                    height={24}
                    style={{ borderRadius: "50%" }}
                  />
                  <span>{room.opponentNickname}</span>
                </div>

                {/* 마지막 메시지 및 시간 */}
                <div style={{ fontSize: "13px", color: "#666" }}>
                  {room.lastMessage} ·{" "}
                  {formatDistanceToNow(new Date(room.lastMessageAt), {
                    addSuffix: true,
                  })}
                </div>

                {/* 읽지 않은 메시지 뱃지 */}
                {room.unreadCount > 0 && (
                  <div
                    style={{
                      marginTop: "4px",
                      backgroundColor: "red",
                      color: "white",
                      borderRadius: "12px",
                      padding: "2px 8px",
                      fontSize: "12px",
                      display: "inline-block",
                      fontWeight: 500,
                    }}
                  >
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
