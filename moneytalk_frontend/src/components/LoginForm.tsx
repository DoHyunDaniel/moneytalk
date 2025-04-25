// src/components/LoginForm.tsx

import { useState } from "react";

/**
 * LoginForm 컴포넌트 Props 타입
 *
 * @param onSubmit 로그인 요청 핸들러 (상위 컴포넌트에서 처리)
 */
interface Props {
  onSubmit: (email: string, password: string) => void;
}

/**
 * 로그인 입력 폼 컴포넌트
 *
 * - 이메일과 비밀번호를 입력받아 onSubmit 콜백을 호출합니다.
 * - 실제 인증 로직은 상위 컴포넌트에서 처리합니다.
 */
const LoginForm = ({ onSubmit }: Props) => {
  const [email, setEmail] = useState("");       // 이메일 입력 상태
  const [password, setPassword] = useState(""); // 비밀번호 입력 상태

  /**
   * 폼 제출 시 호출되는 핸들러
   *
   * - 기본 이벤트 방지
   * - 상위 컴포넌트에 email, password 전달
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(email, password);
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>로그인</h2>

      {/* 이메일 입력 */}
      <input
        placeholder="이메일"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <br />

      {/* 비밀번호 입력 */}
      <input
        type="password"
        placeholder="비밀번호"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <br />

      {/* 로그인 버튼 */}
      <button type="submit">로그인</button>
    </form>
  );
};

export default LoginForm;
