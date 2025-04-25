# 💸 MoneyTalk Frontend

> 사용자 간 중고 상품 거래 및 커뮤니케이션을 지원하는 **React 기반 프론트엔드**
> 백엔드(Spring Boot) API와 연동되어 작동합니다.

![React](https://img.shields.io/badge/React-18-blue)
![Vite](https://img.shields.io/badge/Build-Vite-purple)
![TypeScript](https://img.shields.io/badge/TypeScript-Strict-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-green)
![WebSocket](https://img.shields.io/badge/Realtime-Chat-green)

---

## ✅ 주요 기능

- ✅ 회원가입 / 로그인 (JWT + HttpOnly Cookie 기반)
- ✅ 로그인 상태 전역 관리 (Context API)
- ✅ 소셜 로그인 (Google OAuth2)
- ✅ 상품 목록 / 상품 상세 조회 UI
- ✅ 찜하기 버튼 연동
- ✅ 실시간 채팅방 목록 / 메시지 UI 구현
- ✅ 채팅방 입장 / 메시지 전송 / 이미지 메시지 지원
- ⏳ 가계부 / 예산 챗봇 UI는 이후 구현 예정

---

## 🛠️ 기술 스택

- **React 18** + TypeScript
- **Vite** 빌드 도구
- **React Router v6** (SPA 라우팅)
- **Axios** (API 통신)
- **Context API** (전역 로그인 상태 관리)
- **JWT 기반 인증** (HttpOnly 쿠키 처리)
- **Google OAuth2 로그인** (React Google Login 연동)

---

## 📁 디렉토리 구조

```bash
src/
├── api/              # Axios instance 및 API 함수 정의
├── components/       # UI 컴포넌트 (ChatRoom, ChatRoomList 등)
├── context/          # 로그인 상태 관리 AuthContext
├── hooks/            # 커스텀 훅 (useAuth 등)
├── pages/            # 개별 화면 구성 (LoginPage, ProductList 등)
├── router/           # AppRouter.tsx (전체 라우팅 설정)
├── styles/           # 글로벌 CSS, Skeleton 스타일 등
└── main.tsx          # 진입점
```

---

## 🚀 실행 방법

```bash
# 1. 의존성 설치
npm install

# 2. 개발 서버 실행
npm run dev
```

> 기본 주소: [http://localhost:5173](http://localhost:5173)
> 백엔드는 `http://localhost:8080`에서 실행 필요

---

## 🌐 환경 변수

현재는 `.env` 없이 Axios baseURL에 하드코딩 되어 있습니다:
```ts
axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true
});
```

> 추후 배포 시 `.env`로 관리 예정

---

## 📦 배포 계획

- 프론트엔드: **Vercel** or **Netlify**
- 백엔드: **AWS EC2** or **Render** (REST API 서버)

---

## 🔍 구현된 화면 예시

- ✅ `/chat` : 채팅방 목록 화면
- ✅ `/chat/:roomId` : 채팅 메시지 송수신 UI
- ✅ `/products` : 전체 상품 리스트
- ✅ `/products/:productId` : 상품 상세 정보

---

## 📌 프론트엔드 개발자

| 이름 | 역할 |
|------|------|
| DoHyun Daniel Kim | 프론트엔드 개발, 상태 관리, 실시간 채팅 UI 설계 |

---