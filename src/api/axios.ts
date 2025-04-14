// src/api/axios.ts
import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:8080/api", // 백엔드 주소에 맞게 수정
  withCredentials: true, // 쿠키 포함 필수 설정
});

export default apiClient;
