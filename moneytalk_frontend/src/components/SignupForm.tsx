import { useState } from 'react';
import { signup, getMyInfo, login } from '../api/auth'; // ✅ auth.ts에 맞춰 signup, getMyInfo import
import { suggestNickname } from '../api/suggestNickname';

function SignupForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    try {
      await signup({ email, password, nickname });  // ✅ auth.ts에 맞춰 signup 호출
      await login({ email, password }); // ✅ auth.ts에 맞춰 login 호출
      const myInfo = await getMyInfo(); // ✅ auth.ts에 맞춰 getMyInfo 호출
      alert(`회원가입 성공! ${myInfo.nickname}님 환영합니다!`);
      // 로그인 후 리다이렉트 또는 다른 작업 수행 
      window.location.href = '/home'; // 예시로 홈으로 리다이렉트
    } catch (error: any) {
      const message = error?.response?.data?.message;
      setError(message);

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
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="이메일" />
      <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" />
      <input value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="닉네임" />
      <button onClick={handleSubmit}>회원가입</button>

      {error && <p style={{ color: 'red' }}>{error}</p>}

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
