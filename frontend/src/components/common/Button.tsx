import { type ButtonHTMLAttributes } from "react";

// 버튼의 시각적 스타일을 정의하는 타입
type ButtonVariant = "primary" | "secondary" | "ghost" | "plain";

// Button 컴포넌트의 Props 정의
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  // 버튼 스타일 (기본값: 'primary')
  variant?: ButtonVariant;
  // 추가적인 Tailwind CSS 클래스
  className?: string;
  // 로딩 상태 (true일 때 버튼 비활성화 및 스타일 적용)
  isLoading?: boolean;
  // 버튼 내부 내용
  children: React.ReactNode;
}

/**
 * 범용적으로 사용 가능한 스타일링된 Button 컴포넌트
 * @param variant - 버튼 스타일 ('primary', 'secondary', 'ghost')
 * @param className - button 요소에 적용될 추가 Tailwind CSS 클래스
 * @param isLoading - 로딩 상태 여부
 * @param children - 버튼 내부 내용 (텍스트, 아이콘 등)
 * @param props - 기타 모든 표준 <button> 속성 (onClick, type, disabled 등)
 */
const Button: React.FC<ButtonProps> = ({
  variant = "primary",
  className = "",
  isLoading = false,
  children,
  ...props
}) => {
  // 기본 공통 스타일
  const baseStyle =
    "font-semibold py-2 px-4 rounded-lg transition duration-200 ease-in-out " +
    "flex items-center justify-center space-x-2";

  // variant에 따른 스타일 정의
  const variantStyles = {
    primary:
      "bg-[#4A7CE9] text-white shadow-md hover:bg-[#3568D4] focus:ring-4 focus:ring-[#4A7CE9]/50",
    secondary:
      "bg-gray-200 text-gray-800 shadow-sm hover:bg-gray-300 focus:ring-4 focus:ring-gray-400/50",
    ghost:
      "bg-transparent text-[#4A7CE9]-600 hover:bg-[#3568D4]-50 focus:ring-4 focus:ring-[#4A7CE9]-500/50",
    plain:
      "bg-transparent text-gray-400 text-base hover:bg-gray-300 focus:ring-4 focus:ring-gray-400/50",
  };

  // 비활성화 및 로딩 상태 스타일
  const disabledStyle =
    props.disabled || isLoading
      ? "opacity-50 cursor-not-allowed pointer-events-none"
      : "";

  const combinedClassName = `${baseStyle} ${variantStyles[variant]} ${disabledStyle} ${className}`;

  return (
    <button
      className={combinedClassName}
      disabled={props.disabled || isLoading}
      {...props}
    >
      {/* 로딩 스피너 (선택 사항) */}
      {isLoading && (
        <svg
          className="animate-spin h-5 w-5 mr-3"
          viewBox="0 0 24 24"
          fill="currentColor"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          ></circle>
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      )}
      <span>{children}</span>
    </button>
  );
};

export default Button;
