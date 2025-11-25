import React from "react";

interface ReversedGaugeChartProps {
  value: number; // 0 ~ 100
}

const ReversedGaugeChart: React.FC<ReversedGaugeChartProps> = ({ value }) => {
  // 값 범위 제한 (0~100)
  const clampedValue = Math.min(Math.max(value, 0), 100);

  // 회전 각도 계산: 0% = -90도, 100% = 90도
  const rotation = (clampedValue / 100) * 180 - 90;

  // 극좌표 -> 직교좌표 변환 함수
  const polarToCartesian = (
    centerX: number,
    centerY: number,
    radius: number,
    angleInDegrees: number
  ) => {
    const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180.0;
    return {
      x: centerX + radius * Math.cos(angleInRadians),
      y: centerY + radius * Math.sin(angleInRadians),
    };
  };

  // SVG Arc(호) 그리기 함수
  const describeArc = (
    x: number,
    y: number,
    radius: number,
    startAngle: number,
    endAngle: number
  ) => {
    const start = polarToCartesian(x, y, radius, endAngle);
    const end = polarToCartesian(x, y, radius, startAngle);
    const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";
    const d = [
      "M",
      start.x,
      start.y,
      "A",
      radius,
      radius,
      0,
      largeArcFlag,
      0,
      end.x,
      end.y,
    ].join(" ");
    return d;
  };

  // 차트 설정값
  const cx = 100; // 중심 X
  const cy = 100; // 중심 Y
  const radius = 80; // 반지름
  const strokeWidth = 12; // 두께를 20 -> 12로 줄여서 더 가늘게 변경

  return (
    // 전체 크기를 max-w-[250px] -> max-w-[200px]로 줄임
    <div className="relative w-full max-w-[200px] aspect-[2/1.2] flex items-center justify-center">
      <svg className="w-full h-full" viewBox="0 0 200 120">
        {/* --- 배경 세그먼트 (색상 반전 로직: 초록 -> 노랑 -> 빨강) --- */}

        {/* 1. Good/Safe Zone (0% ~ 50%): Green */}
        <path
          d={describeArc(cx, cy, radius, -90, -2)}
          fill="none"
          stroke="#34D399" // Tailwind green-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* 2. Warning Zone (50% ~ 75%): Yellow */}
        <path
          d={describeArc(cx, cy, radius, 0, 43)}
          fill="none"
          stroke="#FBBF24" // Tailwind amber-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* 3. Risk Zone (75% ~ 100%): Red */}
        <path
          d={describeArc(cx, cy, radius, 45, 90)}
          fill="none"
          stroke="#F87171" // Tailwind red-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* --- 작은 삼각형 포인터 --- */}
        <g transform={`rotate(${rotation}, ${cx}, ${cy})`}>
          {/* 호가 얇아졌으므로(두께 12), 내측 반지름은 74가 됩니다 (80 - 6).
            화살표가 74 지점(호의 안쪽 라인)을 가리키도록 좌표를 조정했습니다.
            Tip: (100, 32) -> y=32는 중심(100)에서 68만큼 떨어짐. (호 바로 앞)
            Base: (97, 40), (103, 40) -> y=40은 중심에서 60만큼 떨어짐.
          */}
          <path
            d="M 100 32 L 97 40 L 103 40 Z"
            fill="#374151" // Dark Gray
          />
        </g>

        {/* --- 텍스트 레이블 --- */}

        {/* 중앙 퍼센트 */}
        <text
          x="100"
          y="95"
          textAnchor="middle"
          fontSize="48"
          fontWeight="semibold"
          fill="#1F2937"
          className="font-sans"
        >
          {Math.round(clampedValue)}
          <tspan fontSize="20" dy="-20">
            %
          </tspan>{" "}
          {/* % 크기 및 위치 조정 */}
        </text>

        {/* 0과 100 레이블 */}
        <text x="20" y="115" fontSize="10" fill="#9CA3AF" textAnchor="middle">
          0
        </text>
        <text x="180" y="115" fontSize="10" fill="#9CA3AF" textAnchor="middle">
          100
        </text>
      </svg>
    </div>
  );
};

export default ReversedGaugeChart;
