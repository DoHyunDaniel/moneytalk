import axios from 'axios';

/**
 * 닉네임 중복 확인 및 추천 API
 *
 * - GET /api/users/nickname/suggest?base={nickname}
 * - 사용자가 입력한 닉네임의 중복 여부를 확인하고, 사용 불가 시 추천 닉네임을 함께 반환합니다.
 *
 * @param base 사용자가 입력한 기본 닉네임
 * @returns {
 *   base: string; // 입력 닉네임
 *   isAvailable: boolean; // 사용 가능 여부
 *   suggestions: string[]; // 대체 추천 닉네임 리스트
 * }
 *
 * 사용 예: 회원가입/닉네임 변경 시 중복 확인 버튼 클릭 → suggestNickname 호출
 */
export const suggestNickname = async (base: string) => {
  const response = await axios.get(`/api/users/nickname/suggest?base=${base}`);
  return response.data; // { base, isAvailable, suggestions }
};
