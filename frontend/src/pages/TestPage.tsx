import { useState, useRef } from "react"; // useRef 추가
import Input from "../components/common/Input";
import Button from "../components/common/Button"; // Button 컴포넌트 임포트

// 이 파일은 Input 및 Button 컴포넌트가 Tailwind CSS 스타일과
// 기본적인 React 로직(state 연결, ref 전달 등)을 잘 처리하는지 확인하기 위한 용도입니다.

const TestPage = () => {
  const [textValue, setTextValue] = useState("");
  const [passwordValue, setPasswordValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Input 컴포넌트의 Ref 테스트를 위한 Ref 객체
  const inputRef = useRef<HTMLInputElement>(null);

  // 로딩 상태를 토글하는 핸들러
  const handleLoadingToggle = () => {
    setIsLoading(true);
    setTimeout(() => setIsLoading(false), 2000); // 2초 후 로딩 해제
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center p-8">
      <h1 className="text-3xl font-extrabold text-indigo-700 mb-10 border-b-2 pb-2">
        Component Test Playground (Input & Button)
      </h1>

      <div className="w-full max-w-lg space-y-8 bg-white p-8 rounded-xl shadow-2xl">
        {/* === INPUT COMPONENT TEST === */}
        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-gray-900 border-b pb-2">
            Input 컴포넌트 테스트
          </h2>

          {/* 1. 기본 텍스트 입력 테스트 (레이블 포함) */}
          <Input
            id="test-text"
            label="고객 이름 검색"
            type="text"
            placeholder="여기에 검색어를 입력하세요..."
            value={textValue}
            onChange={(e) => setTextValue(e.target.value)}
            // ref={inputRef} // ref 테스트 시 사용
          />
          <p className="mt-2 text-sm text-gray-600">
            입력된 값:{" "}
            <span className="font-mono bg-indigo-50 text-indigo-700 p-1 rounded text-xs">
              {textValue || "없음"}
            </span>
          </p>

          {/* 2. 비밀번호 입력 테스트 */}
          <Input
            id="test-password"
            label="비밀번호"
            type="password"
            placeholder="비밀번호를 입력하세요"
            value={passwordValue}
            onChange={(e) => setPasswordValue(e.target.value)}
          />

          {/* 3. 비활성화된 입력 필드 */}
          <Input
            id="test-disabled"
            label="비활성화된 필드"
            type="text"
            defaultValue="이 값은 변경할 수 없습니다."
            disabled
            className="bg-gray-100 cursor-not-allowed"
          />
        </section>

        {/* --- BUTTON COMPONENT TEST --- */}
        <section className="space-y-4 pt-4">
          <h2 className="text-2xl font-bold text-gray-900 border-b pb-2">
            Button 컴포넌트 테스트
          </h2>

          <div className="flex flex-col space-y-4 sm:flex-row sm:space-y-0 sm:space-x-4">
            {/* Primary Button */}
            <Button
              variant="primary"
              onClick={handleLoadingToggle}
              disabled={isLoading}
            >
              Primary (로그인)
            </Button>

            {/* Secondary Button */}
            <Button variant="secondary">Secondary (취소)</Button>

            {/* Ghost Button */}
            <Button variant="ghost">Ghost (자세히)</Button>
          </div>

          <div className="flex space-x-4 pt-2">
            {/* Loading Button */}
            <Button variant="primary" isLoading={isLoading}>
              {isLoading ? "로딩 중..." : "로딩 버튼"}
            </Button>

            {/* Disabled Button */}
            <Button variant="primary" disabled={true}>
              비활성화됨
            </Button>
          </div>
        </section>
      </div>
    </div>
  );
};

export default TestPage;
