// src/types/authTypes.ts

/**
 * 로그인 요청 시 백엔드에 전송되는 사용자 자격 증명 타입 정의
 */
export interface AuthCredentials {
  username: string; // 사용자 ID (사번, 이메일 등)
  password: string; // 비밀번호
}

// 필요에 따라 AuthResponse (로그인 성공 응답) 등 다른 인증 관련 타입도 정의될 수 있습니다.
