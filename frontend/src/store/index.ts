import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./authSlice";

// Redux Store 설정
export const store = configureStore({
  reducer: {
    // 모든 slice들을 여기에 등록합니다.
    auth: authReducer,
    // customer: customerReducer, // 추후 추가 예정
    // dashboard: dashboardReducer, // 추후 추가 예정
  },
  // Redux Toolkit 기본 미들웨어 설정은 그대로 사용합니다.
});

// AppDispatch와 RootState 타입을 내보내어 TypeScript 환경에서 사용합니다.
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
