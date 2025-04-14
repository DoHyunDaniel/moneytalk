// src/pages/SignupPage.tsx
import { signup } from "../api/auth";
import { useNavigate } from "react-router-dom";
import SignupForm from "../components/SignupForm";

const SignupPage = () => {
  const navigate = useNavigate();

  const handleSignup = async (email: string, password: string, nickname: string) => {
    try {
      const res = await signup({ email, password, nickname });
      alert(`${res.nickname}님, 회원가입이 완료되었습니다!`);
      navigate("/login"); // 회원가입 후 로그인 페이지로 이동
    } catch (error: any) {
      alert("회원가입 실패: " + error.response?.data?.message);
    }
  };

  return (
    <div>
      <SignupForm onSubmit={handleSignup} />
    </div>
  );
};

export default SignupPage;
