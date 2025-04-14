# 💸 MoneyTalk Frontend

MoneyTalk는 사용자 간 중고 상품 거래 및 커뮤니케이션을 지원하는 플랫폼입니다.  
이 리포지토리는 React 기반 프론트엔드 코드로, 백엔드(Spring Boot)와 연동되어 작동합니다.

---

## 🛠️ 기술 스택

- **React 18** with TypeScript
- **Vite**
- **React Router v6**
- **Axios**
- **Context API** – 로그인 상태 전역 관리
- **JWT + HttpOnly Cookie 인증**
- **OAuth2 (Google 소셜 로그인)**

---

## 📁 폴더 구조

```bash
moneytalk_frontend/
├── src/
│   ├── api/              # Axios instance 및 API 모듈
│   ├── components/       # 공통 UI 컴포넌트 (Header, Form 등)
│   ├── context/          # AuthContext (전역 로그인 상태 관리)
│   ├── hooks/            # 커스텀 훅 (useAuth 등)
│   ├── pages/            # 라우팅되는 개별 페이지들
│   ├── router/           # AppRouter (React Router 설정)
│   ├── styles/           # 글로벌 CSS
│   └── main.tsx          # 앱 진입점
```

---

## 🚀 주요 기능

- ✅ 회원가입 / 로그인
- ✅ JWT 기반 인증 처리
- ✅ 로그인 상태 헤더에 반영
- ✅ 로그아웃 기능
- ✅ 소셜 로그인 (Google) 연동
- ⏳ 찜 목록 / 구매 내역 / 리뷰 기능 개발 중...

---

## 🧪 개발 서버 실행

```bash
# 1. 의존성 설치
npm install

# 2. 개발 서버 실행
npm run dev
```

> 기본 주소: [http://localhost:5173](http://localhost:5173)  
> 백엔드는 `http://localhost:8080`에서 Spring Boot가 실행되어야 합니다.

---

## 🌐 환경 변수

현재는 별도 `.env` 설정 없이 Axios `baseURL`에 `http://localhost:8080/api`가 하드코딩되어 있습니다.  
추후 배포 시 `.env` 파일로 관리 예정입니다.

---

## 📦 배포 계획

- 프론트: Vercel 또는 Netlify
- 백엔드: AWS EC2 or Render

---

## 📌 개발자

| 이름 | 역할 |
|------|------|
| DoHyun Daniel Kim | Frontend & Backend 개발, 전체 아키텍처 설계 |

---
