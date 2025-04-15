import { Link } from "react-router-dom";
import LoginForm from "../components/LoginForm";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (email: string, password: string) => {
    try {
      await login(email, password);
      navigate("/home");
    } catch (error: any) {
      alert("로그인 실패: " + error.response?.data?.message);
    }
  };

  return (
    <div>
      <LoginForm onSubmit={handleLogin} />
      <p>
        계정이 없으신가요? <Link to="/signup">회원가입</Link>
      </p>

      <hr />
      <a href="http://localhost:8080/oauth2/authorization/google">
        <button>구글로 로그인</button>
      </a>
    </div>
  );
};

export default LoginPage;
// src/pages/LoginPage.tsx
// import { Link } from "react-router-dom";