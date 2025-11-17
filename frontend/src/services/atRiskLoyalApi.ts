import axios, { type AxiosInstance } from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000, // 10초 타임아웃
});

export const getDeclineCustomers = async () => {
  try {
    const token = localStorage.getItem("authToken");

    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await api.get("/stores/customers/decline", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const serverMessage =
        error.response.data.message || "이탈 위험 충성 고객 정보 서버 오류";
      throw new Error(serverMessage);
    }
    throw new Error("네트워크 연결 또는 서버 응답에 문제가 발생했습니다.");
  }
};
