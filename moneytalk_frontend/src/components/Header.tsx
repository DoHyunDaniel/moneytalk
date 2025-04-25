import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * 전역 헤더 컴포넌트
 *
 * - 사용자 로그인 여부에 따라 다른 메뉴를 렌더링합니다.
 * - 로그인 상태: 닉네임 + 로그아웃 버튼
 * - 로그아웃 상태: 로그인 / 회원가입 링크
 */
const Header = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  /**
   * 로그아웃 핸들러
   * - 로그아웃 API 호출 후 /login 페이지로 이동
   */
  const handleLogout = async () => {
    await logout();
    alert("로그아웃되었습니다.");
    navigate("/login");
  };

  return (
    <header style={{ padding: "1rem", borderBottom: "1px solid #ccc" }}>
      {/* 로고 또는 홈 링크 */}
      <h1 style={{ display: "inline-block", marginRight: "2rem" }}>
        <Link to="/home">MoneyTalk</Link>
      </h1>

      {/* 로그인 상태일 경우: 사용자 환영 메시지 + 로그아웃 */}
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
        // 비로그인 상태일 경우: 로그인 / 회원가입 링크
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
