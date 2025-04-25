import { useState } from 'react';
import { signup, getMyInfo, login } from '../api/auth'; // 인증 관련 API
import { suggestNickname } from '../api/suggestNickname';

/**
 * 회원가입 폼 컴포넌트
 *
 * - 이메일, 비밀번호, 닉네임 입력
 * - 회원가입 → 로그인 → 유저 정보 조회 → 홈으로 이동
 * - 닉네임 중복 시 추천 닉네임을 표시
 */
function SignupForm() {
  // 입력값 상태
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');

  // 추천 닉네임 리스트
  const [suggestions, setSuggestions] = useState<string[]>([]);

  // 에러 메시지 상태
  const [error, setError] = useState('');

  /**
   * 회원가입 제출 핸들러
   *
   * 1. signup → 2. login → 3. getMyInfo
   * 4. 홈으로 리다이렉트
   * 5. 닉네임 중복 에러 시 추천 닉네임 표시
   */
  const handleSubmit = async () => {
    try {
      await signup({ email, password, nickname });  // 회원가입 요청
      await login({ email, password });              // 로그인 요청
      const myInfo = await getMyInfo();              // 로그인한 사용자 정보 조회
      alert(`회원가입 성공! ${myInfo.nickname}님 환영합니다!`);
      window.location.href = '/home';                // 홈으로 이동
    } catch (error: any) {
      const message = error?.response?.data?.message;
      setError(message); // 에러 메시지 화면에 출력

      // 닉네임 중복 시 닉네임 추천 API 호출
      if (message?.includes('닉네임')) {
        const res = await suggestNickname(nickname);
        if (!res.isAvailable) {
          setSuggestions(res.suggestions);
        }
      }
    }
  };

  return (
    <div>
      {/* 이메일 입력 */}
      <input
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
      />

      {/* 비밀번호 입력 */}
      <input
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="비밀번호"
        type="password"
      />

      {/* 닉네임 입력 */}
      <input
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
        placeholder="닉네임"
      />

      {/* 회원가입 버튼 */}
      <button onClick={handleSubmit}>회원가입</button>

      {/* 에러 메시지 표시 */}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {/* 닉네임 중복 시 추천 닉네임 렌더링 */}
      {suggestions.length > 0 && (
        <div>
          <p>이 닉네임은 어떤가요?</p>
          <ul>
            {suggestions.map((sug, i) => (
              <li key={i}>
                <button onClick={() => setNickname(sug)}>{sug}</button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default SignupForm;
