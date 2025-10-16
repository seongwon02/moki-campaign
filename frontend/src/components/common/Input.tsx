import { type InputHTMLAttributes, forwardRef } from "react";

// 1. Input 컴포넌트가 받을 props의 타입 정의
type InputVariant = "default" | "underline";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  // 컴포넌트에 전달할 추가적인 Tailwind CSS 클래스
  className?: string;
  // 입력 필드에 표시될 레이블 (선택 사항)
  label?: string;
  // 입력 필드 스타일 variant
  variant?: InputVariant;
}

/**
 * 범용적으로 사용 가능한 스타일링된 Input 컴포넌트
 * @param label - 입력 필드 상단에 표시될 레이블
 * @param className - input 요소에 적용될 추가 Tailwind CSS 클래스
 * @param variant - 'default' 또는 'underline' 스타일
 * @param props - 기타 모든 표준 <input> 속성 (type, value, onChange, placeholder 등)
 */
const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, className = "", variant = "default", ...props }, ref) => {
    const baseStyle =
      "w-full transition duration-150 ease-in-out focus:outline-none";

    const variantStyles = {
      default:
        "p-3 border border-gray-300 rounded-lg shadow-sm " +
        "focus:border-indigo-500 focus:ring-indigo-500",
      underline:
        "px-1 py-2 bg-transparent border-0 border-b-2 border-gray-300 rounded-none shadow-none " +
        "focus:ring-0 focus:border-indigo-500",
    };

    const combinedClassName = `${baseStyle} ${variantStyles[variant]} ${className}`;

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
