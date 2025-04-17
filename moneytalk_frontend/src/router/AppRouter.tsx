// src/router/AppRouter.tsx
import { Routes, Route } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import SignupPage from "../pages/SignupPage";
import HomePage from "../pages/HomePage";
import ChatRoom from "../components/ChatRoom";

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/home" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/chat" element={<ChatRoom />} />
      <Route path="*" element={<div>404 Not Found</div>} />
    </Routes>
  );
};

export default AppRouter;
