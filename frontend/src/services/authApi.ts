import axios, { type AxiosInstance } from "axios";
import { type AuthCredentials } from "../types/authTypes.ts";

// 환경 변수에서 기본 API URL을 가져옵니다.
// .env 파일에 VITE_API_BASE_URL="http://localhost:8080/api" 와 같이 설정되어 있어야 합니다.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

/**
 * Axios 인스턴스 설정
 * 모든 인증 관련 API 호출에 사용됩니다.
 */
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000, // 10초 타임아웃
});

/**
 * 로그인 요청
 * 백엔드 서버에 사용자 인증 정보를 전송하고 토큰을 받습니다.
 * * @param credentials - 사용자 ID 및 비밀번호 ({ username, password })
 * @returns {Promise<{ token: string }>} - 인증 성공 시 토큰을 포함한 응답
 */
export const login = async (
  credentials: AuthCredentials
): Promise<{ token: string }> => {
  try {
    // 실제 로그인 엔드포인트는 /auth/login 또는 /login 이라고 가정합니다.
    const response = await api.post("/auth/login", {
      username: credentials.username,
      password: credentials.password,
    });

    // 백엔드 응답 구조에 따라 토큰을 추출합니다. (예: response.data.accessToken)
    // 여기서는 응답 데이터에 token 필드가 있다고 가정합니다.
    const token = response.data.token;

    if (!token) {
      throw new Error("API 응답에서 토큰을 찾을 수 없습니다.");
    }

    // 성공적으로 토큰을 반환합니다.
    return { token };
  } catch (error) {
    // Axios 에러 처리: 상세 에러 메시지를 추출하여 던집니다.
    if (axios.isAxiosError(error) && error.response) {
      // 서버에서 보낸 에러 메시지가 있을 경우 사용
      const serverMessage = error.response.data.message || "로그인 서버 오류";
      throw new Error(serverMessage);
    }

    // 네트워크 오류 또는 기타 오류
    throw new Error("네트워크 연결 또는 서버 응답에 문제가 발생했습니다.");
  }
};
