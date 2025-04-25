import axios from 'axios';

export const suggestNickname = async (base: string) => {
    const response = await axios.get(`/api/users/nickname/suggest?base=${base}`);
    return response.data; // { base, isAvailable, suggestions }
  };
  