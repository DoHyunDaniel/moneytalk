import apiClient from "./axios";

/**
 * 전체 상품 목록을 조회합니다.
 *
 * - GET /products
 * - 검색 조건이 없을 경우 모든 상품이 반환됩니다.
 *
 * @returns ProductResponseDto[] 상품 목록
 */
export const getAllProducts = async () => {
  const res = await apiClient.get("/products");
  return res.data;
};

/**
 * 특정 상품의 상세 정보를 조회합니다.
 *
 * - GET /products/{productId}
 * - 상품 제목, 설명, 이미지, 판매자 정보 등이 포함됩니다.
 *
 * @param productId 조회할 상품의 ID
 * @returns ProductResponseDto
 */
export const getProductById = async (productId: number) => {
  const res = await apiClient.get(`/products/${productId}`);
  return res.data;
};

/**
 * 상품을 기준으로 채팅방을 생성합니다.
 *
 * - POST /chatrooms/{productId}
 * - 이미 존재하는 채팅방이 있다면 해당 채팅방 정보를 반환합니다.
 *
 * @param productId 채팅을 시작할 상품의 ID
 * @returns ChatRoomResponseDto
 */
export const createChatRoom = async (productId: number) => {
  return apiClient.post(`/chatrooms/${productId}`);
};
