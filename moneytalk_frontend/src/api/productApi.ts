import apiClient from "./axios";

export const getAllProducts = async () => {
  const res = await apiClient.get("/products");
  return res.data;
};

export const getProductById = async (productId: number) => {
  const res = await apiClient.get(`/products/${productId}`);
  return res.data;
};

export const createChatRoom = async (productId: number) => {
    return apiClient.post(`/chatrooms/${productId}`);
};
