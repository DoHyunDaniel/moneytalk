// src/api/axios.ts

import axios from "axios";

/**
 * Axios 클라이언트 설정
 *
 * - baseURL: 백엔드 API 서버의 기본 주소
 *   (개발 시 http://localhost:8080/api, 배포 시 환경변수로 분리 권장)
 *
 * - withCredentials: httpOnly 쿠키 전송을 위해 반드시 true 설정
 *   (로그인 토큰이 쿠키에 저장되는 구조이므로 이 설정이 없으면 인증이 작동하지 않음)
 */
const apiClient = axios.create({
  baseURL: "http://localhost:8080/api", // TODO: 배포 시 환경변수로 분리
  withCredentials: true, // 쿠키 기반 인증을 위한 필수 설정
});

export default apiClient;
