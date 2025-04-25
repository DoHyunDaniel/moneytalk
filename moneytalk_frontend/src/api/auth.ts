import apiClient from "./axios";

/**
 * 로그인 요청 시 전달되는 데이터 형태
 */
interface LoginRequest {
  email: string;
  password: string;
}

/**
 * 회원가입 요청 시 전달되는 데이터 형태
 */
interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

/**
 * 로그인 API
 * 
 * - POST /users/login
 * - 서버에서 email, nickname을 JSON으로 반환하며,
 *   인증 토큰은 httpOnly 쿠키로 내려옴
 * 
 * @param data 로그인 요청 데이터 (email, password)
 * @returns { email: string, nickname: string }
 */
export const login = async (data: LoginRequest) => {
  const response = await apiClient.post("/users/login", data);
  return response.data; // email, nickname 리턴됨 (token은 httpOnly cookie로 내려옴)
};

/**
 * 회원가입 API
 * 
 * - POST /users/signup
 * - 회원가입 성공 시 생성된 유저 정보(email, nickname 등) 반환
 * 
 * @param data 회원가입 요청 데이터 (email, password, nickname)
 * @returns 생성된 유저 정보
 */
export const signup = async (data: SignupRequest) => {
  const response = await apiClient.post("/users/signup", data);
  return response.data;
};

/**
 * 내 정보 조회 API
 * 
 * - GET /users/me
 * - httpOnly 쿠키 기반 인증 필요
 * - 로그인된 유저의 email, nickname, userId 등을 반환
 * 
 * @returns 현재 로그인된 유저 정보
 */
export const getMyInfo = async () => {
  const response = await apiClient.get("/users/me");
  return response.data;
};

/**
 * 로그아웃 API
 * 
 * - POST /users/logout
 * - 서버에서 httpOnly 쿠키 삭제 처리됨
 * - 별도 응답 데이터 없음 (204 No Content 또는 OK)
 */
export const logout = async () => {
  await apiClient.post("/users/logout");
};
