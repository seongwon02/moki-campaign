import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { loginUser, resetStatus } from "../store/authSlice.ts";
import { type AppDispatch, type RootState } from "../store/index.ts";
import Input from "../components/common/Input.tsx";
import Button from "../components/common/Button.tsx";

/**
 * 로그인 페이지 컴포넌트
 * 인증 상태(Redux)를 통해 로그인 처리 및 페이지 이동을 담당합니다.
 */
const LoginPage: React.FC = () => {
  const [username, setUsername] = useState(""); // 테스트를 위한 기본값 설정
  const [password, setPassword] = useState(""); // 테스트를 위한 기본값 설정
  const [localError, setLocalError] = useState<string | null>(null);

  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const { status, error: reduxError } = useSelector(
    (state: RootState) => state.auth
  );
  const isLoading = status === "loading";

  // Redux 에러 발생 시 로컬 에러 상태 업데이트
  useEffect(() => {
    if (reduxError) {
      setLocalError(reduxError);
    }
  }, [reduxError]);

  // 페이지 언마운트 시 에러 상태 초기화
  useEffect(() => {
    return () => {
      dispatch(resetStatus());
    };
  }, [dispatch]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);

    if (!username || !password) {
      setLocalError("ID와 비밀번호를 모두 입력해주세요.");
      return;
    }

    // Redux Thunk 디스패치
    const resultAction = await dispatch(loginUser({ username, password }));

    // 로그인 성공 시 메인 대시보드로 이동
    if (loginUser.fulfilled.match(resultAction)) {
      navigate("/");
    }
    // 실패 시 에러 메시지는 Redux state를 통해 localError에 이미 반영됨
  };

  return (
    // 배경색을 커스텀 CSS 변수로 설정 (Tailwind bg-gray-100 대신)
    <div
      className="flex items-center justify-center min-h-screen p-4"
      style={{ backgroundColor: "var(--color-bg-light)" }}
    >
      <div className="w-full max-w-md p-8 space-y-8 rounded-xl">
        {/* 로고 및 앱 이름 */}
        <div className="flex flex-col items-center justify-center space-y-2 pb-4">
          {/* 이미지 로고: W-200, H-100 크기 스타일을 추가했습니다. */}
          <img
            src="/src/assets/moki_logo.svg"
            alt="앱 로고"
            className="w-200 h-100"
          />
        </div>

        <form className="space-y-6" onSubmit={handleSubmit}>
          {/* ID 입력 필드 */}
          <Input
            id="username"
            type="text"
            label="아이디(사업자 번호)"
            placeholder="아이디(사업자 번호)를 입력해주세요"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            variant="underline"
          />

          {/* 비밀번호 입력 필드 */}
          <Input
            id="password"
            type="password"
            label="비밀번호"
            placeholder="비밀번호를 입력해주세요"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            variant="underline"
          />

          {/* 에러 메시지 표시 */}
          {localError && (
            <div className="p-3 text-sm text-red-700 bg-red-100 rounded-lg border border-red-300 transition-all duration-300">
              {localError}
            </div>
          )}

          {/* 로그인 버튼 */}
          <Button type="submit" isLoading={isLoading} className="w-full h-12">
            {isLoading ? "로그인 중..." : "로그인"}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
