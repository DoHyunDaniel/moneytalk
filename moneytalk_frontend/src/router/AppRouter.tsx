import { Routes, Route } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import SignupPage from "../pages/SignupPage";
import HomePage from "../pages/HomePage";
import ChatRoomList from "../components/ChatRoomList";
import ChatRoom from "../components/ChatRoom";
import ProductList from "../pages/ProductList";
import ProductDetail from "../pages/ProductDetail";

const AppRouter = () => {
  return (
    <Routes>
      {/* 기본 라우팅 */}
      <Route path="/home" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      {/* 채팅 관련 */}
      <Route path="/chat" element={<ChatRoomList />} />
      <Route path="/chat/:roomId" element={<ChatRoom />} />

      {/* 상품 관련 */}
      <Route path="/products" element={<ProductList />} />
      <Route path="/products/:productId" element={<ProductDetail />} />

      {/* 404 */}
      <Route path="*" element={<div>404 Not Found</div>} />
    </Routes>
  );
};

export default AppRouter;
