import { type InputHTMLAttributes, forwardRef } from "react";

// 1. Input 컴포넌트가 받을 props의 타입 정의
// InputHTMLAttributes<HTMLInputElement>를 확장하여
// placeholder, type, onChange 등 모든 표준 <input> 속성을 상속받습니다.
interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  // 컴포넌트에 전달할 추가적인 Tailwind CSS 클래스
  className?: string;
  // 입력 필드에 표시될 레이블 (선택 사항)
  label?: string;
}

/**
 * 범용적으로 사용 가능한 스타일링된 Input 컴포넌트
 * @param label - 입력 필드 상단에 표시될 레이블
 * @param className - input 요소에 적용될 추가 Tailwind CSS 클래스
 * @param props - 기타 모든 표준 <input> 속성 (type, value, onChange, placeholder 등)
 */
const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, className = "", ...props }, ref) => {
    // 기본 Tailwind CSS 스타일 정의
    // - w-full: 부모 컨테이너 너비 전체 사용
    // - p-3: 적절한 패딩
    // - border: 경계선
    // - rounded-lg: 둥근 모서리
    // - focus:ring-2: 포커스 시 링 효과
    const baseStyle =
      "w-full p-3 border border-gray-300 rounded-lg shadow-sm " +
      "focus:outline-none focus:border-indigo-500 focus:ring-indigo-500 transition duration-150 ease-in-out";

    const combinedClassName = `${baseStyle} ${className}`;

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={props.id}
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            {label}
          </label>
        )}
        <input ref={ref} className={combinedClassName} {...props} />
      </div>
    );
  }
);

// 컴포넌트 디버깅 시 이름 표시를 위해 displayName 설정
Input.displayName = "Input";

export default Input;
