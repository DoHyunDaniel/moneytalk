import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Header = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    alert("로그아웃되었습니다.");
    navigate("/login");
  };

  return (
    <header style={{ padding: "1rem", borderBottom: "1px solid #ccc" }}>
      <h1 style={{ display: "inline-block", marginRight: "2rem" }}>
        <Link to="/home">MoneyTalk</Link>
      </h1>

      {user ? (
        <>
          <span style={{ marginRight: "1rem" }}>
            안녕하세요, {user.nickname}님!
          </span>
          <button onClick={handleLogout} style={{ cursor: "pointer" }}>
            로그아웃
          </button>
        </>
      ) : (
        <>
          <Link to="/login" style={{ marginRight: "1rem" }}>
            로그인
          </Link>
          <Link to="/signup">회원가입</Link>
        </>
      )}
    </header>
  );
};

export default Header;
