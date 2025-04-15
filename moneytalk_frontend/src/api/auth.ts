import apiClient from "./axios";

interface LoginRequest {
  email: string;
  password: string;
}

interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export const login = async (data: LoginRequest) => {
  const response = await apiClient.post("/users/login", data);
  return response.data; // email, nickname 리턴됨 (token은 httpOnly cookie로 내려옴)
};

export const signup = async (data: SignupRequest) => {
  const response = await apiClient.post("/users/signup", data);
  return response.data;
};

export const getMyInfo = async () => {
  const response = await apiClient.get("/users/me");
  return response.data;
};

export const logout = async () => {
    await apiClient.post("/users/logout");
  };
  