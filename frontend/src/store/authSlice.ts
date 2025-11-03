import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { type AuthCredentials } from "../types/authTypes.ts"; // 타입 정의 임포트
import { login } from "../services/authApi.ts"; // 실제 API 서비스 임포트

// 상태 타입 정의 (State Shape)
interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
}

const initialState: AuthState = {
  // 로컬 스토리지에서 기존 토큰을 로드
  token: localStorage.getItem("authToken") || null,
  isAuthenticated: !!localStorage.getItem("authToken"),
  status: "idle",
  error: null,
};

// 비동기 로그인 썽크 (Thunk)
export const loginUser = createAsyncThunk<
  string, // 최종 성공 시 반환되는 타입 (토큰)
  AuthCredentials, // 썽크에 전달되는 인자 타입 (자격 증명: { businessNumber, password })
  { rejectValue: string } // 실패 시 rejectValue 타입 (API에서 받은 에러 메시지)
>("auth/login", async (credentials, { rejectWithValue }) => {
  try {
    // 새로 작성된 실제 API 호출
    const response = await login(credentials);
    const { token } = response;

    // 토큰 저장 (Local Storage와 Redux 상태)
    localStorage.setItem("authToken", token);

    return token;
  } catch (error) {
    // authApi.ts에서 던진 Error 객체에서 메시지를 추출하여 반환
    const errorMessage =
      error instanceof Error ? error.message : "알 수 없는 로그인 오류 발생";

    // 에러 메시지를 리듀서로 전달
    return rejectWithValue(errorMessage);
  }
});

export const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    // 로그아웃 액션
    logout: (state) => {
      state.token = null;
      state.isAuthenticated = false;
      state.status = "idle";
      state.error = null;
      localStorage.removeItem("authToken");
    },
    // 상태 초기화 액션 (로그인 페이지에서 에러 메시지 초기화용)
    resetStatus: (state) => {
      state.status = "idle";
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // 요청 시작
      .addCase(loginUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      // 요청 성공
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload;
        state.isAuthenticated = true;
        state.error = null;
      })
      // 요청 실패
      .addCase(loginUser.rejected, (state, action) => {
        state.status = "failed";
        // rejectWithValue로 전달된 에러 메시지 사용
        state.error = action.payload || "로그인 실패";
        state.isAuthenticated = false;
        state.token = null;
        localStorage.removeItem("authToken");
      });
  },
});

export const { logout, resetStatus } = authSlice.actions;

export default authSlice.reducer;
