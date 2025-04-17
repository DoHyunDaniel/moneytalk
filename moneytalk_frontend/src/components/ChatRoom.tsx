import { useEffect, useState } from 'react';
import {
  connectChatSocket,
  sendChatMessage,
} from '../api/chatSocket';

const ChatRoom = () => {
  const [messages, setMessages] = useState<string[]>([]);
  const [input, setInput] = useState('');

  const roomId = 1;
  const senderId = 3;
  const senderNickname = '민수';

  useEffect(() => {
    connectChatSocket(roomId, (msg) => {
      setMessages((prev) => [
        ...prev,
        msg.imageUrl
          ? `[${msg.senderNickname}] [이미지: ${msg.imageUrl}]`
          : `[${msg.senderNickname}] ${msg.message}`,
      ]);
    });
  }, []);

  const handleSend = () => {
    if (!input.trim()) return;
    sendChatMessage({
      chatRoomId: roomId,
      senderId,
      senderNickname,
      message: input,
      type: 'TEXT',
    });
    setInput('');
  };

  return (
    <div>
      <h2>💬 채팅방</h2>
      <ul>
        {messages.map((m, i) => (
          <li key={i}>{m}</li>
        ))}
      </ul>
      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="메시지 입력"
      />
      <button onClick={handleSend}>보내기</button>
    </div>
  );
};

export default ChatRoom;