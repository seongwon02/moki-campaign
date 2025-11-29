import React, { useEffect, useState } from "react";

interface ReversedGaugeChartProps {
  value: number; // 0 ~ 100
}

const ReversedGaugeChart: React.FC<ReversedGaugeChartProps> = ({ value }) => {
  const [animatedValue, setAnimatedValue] = useState(0);

  useEffect(() => {
    const startValue = 0;
    const peakValue = 100;
    const finalValue = Math.min(Math.max(value, 0), 100);

    // Phase 1: 0 -> 100 (빠르게 올라감, 0.8초로 수정)
    const phase1Duration = 800;
    // Phase 2: 100 -> 목표값 (부드럽게 안착, 1.2초로 수정)
    const phase2Duration = 1200;
    const totalDuration = phase1Duration + phase2Duration;

    let startTime: number | null = null;

    const animate = (currentTime: number) => {
      if (startTime === null) startTime = currentTime;
      const timeElapsed = currentTime - startTime;

      let currentValue = startValue;

      if (timeElapsed < phase1Duration) {
        // [1단계] 0 -> 100 (상승)
        const progress = timeElapsed / phase1Duration;
        // EaseOutCubic: 끝에서 천천히
        const ease = 1 - Math.pow(1 - progress, 3);
        currentValue = startValue + (peakValue - startValue) * ease;
      } else {
        // [2단계] 100 -> 목표값 (하강/안착)
        const progress = Math.min(
          (timeElapsed - phase1Duration) / phase2Duration,
          1
        );
        // EaseOutCubic: 목표값에 부드럽게 멈춤
        const ease = 1 - Math.pow(1 - progress, 3);
        currentValue = peakValue + (finalValue - peakValue) * ease;
      }

      setAnimatedValue(currentValue);

      if (timeElapsed < totalDuration) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);

    // Cleanup
    return () => {
      startTime = null;
    };
  }, [value]);

  // 회전 각도 계산: 3/4원 (270도)
  // 0% = -135도 (7시 방향), 100% = 135도 (5시 방향)
  // animatedValue를 사용하여 애니메이션 적용
  const rotation = (animatedValue / 100) * 270 - 135;

  // 극좌표 -> 직교좌표 변환 함수
  const polarToCartesian = (
    centerX: number,
    centerY: number,
    radius: number,
    angleInDegrees: number
  ) => {
    // SVG 좌표계에서 -90도가 12시 방향(0 rad) 기준
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
  const cy = 90; // 중심 Y (높이 확보를 위해 살짝 위로)
  const radius = 80; // 반지름
  const strokeWidth = 12; // 두께

  return (
    // 높이가 늘어났으므로 aspect ratio 조정 (2/1.6 정도)
    <div className="relative w-full max-w-[120px] aspect-[2/1.6] flex items-center justify-center">
      <svg className="w-full h-full" viewBox="0 0 200 160">
        {/* --- 배경 세그먼트 (색상 반전 로직: 초록 -> 노랑 -> 빨강) --- 
            전체 범위: -135도 ~ 135도 (총 270도)
            1. Good (0~10%): -135 ~ 0도 (135도 구간)
            2. Warning (10~30%): 0 ~ 67.5도 (67.5도 구간)
            3. Risk (30~100%): 67.5 ~ 135도 (67.5도 구간)
        */}

        {/* 1. Good/Safe Zone (0% ~ 10%): Green */}
        <path
          d={describeArc(cx, cy, radius, -135, -110)} // -108도 직전까지
          fill="none"
          stroke="#34D399" // Tailwind green-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* 2. Warning Zone (50% ~ 75%): Yellow */}
        <path
          d={describeArc(cx, cy, radius, -106, -56)} // -108도 직후 ~ -54도 직전
          fill="none"
          stroke="#FBBF24" // Tailwind amber-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* 3. Risk Zone (75% ~ 100%): Red */}
        <path
          d={describeArc(cx, cy, radius, -52, 135)} // -54도 직후 ~ 135도
          fill="none"
          stroke="#F87171" // Tailwind red-400
          strokeWidth={strokeWidth}
          strokeLinecap="butt"
        />

        {/* --- 작은 삼각형 포인터 --- */}
        {/* rotation 값이 애니메이션에 따라 변경됨 */}
        <g transform={`rotate(${rotation}, ${cx}, ${cy})`}>
          {/* 회전 중심이 (cx, cy)로 변경됨
            화살표 위치 조정: (cx, cy-radius+offset) 
            cy=90 이므로, 12시 방향 기준 좌표 재계산 필요.
            Top(12시)은 y = 90 - 80 = 10.
            내측 라인(반지름 74) 지점 = y 16.
            M 100 22 (tip) -> 반지름 68
            Base at y 30 -> 반지름 60
          */}
          <path
            d="M 100 22 L 97 30 L 103 30 Z"
            fill="#374151" // Dark Gray
          />
        </g>

        {/* --- 텍스트 레이블 --- */}

        {/* 중앙 퍼센트: animatedValue를 사용하여 같이 애니메이션 적용 */}
        <text
          x="100"
          y="110" // 중심보다 약간 아래
          textAnchor="middle"
          fontSize="55"
          fontWeight="bold"
          fill="#1F2937"
          className="font-sans"
        >
          {Math.round(animatedValue)}
          <tspan fontSize="20" dy="-32">
            %
          </tspan>
        </text>

        {/* 0과 100 레이블 (위치 조정됨) */}
        {/* 각도 -135도, 135도 끝점에 맞춰 배치 */}
        <text x="55" y="155" fontSize="12" fill="#9CA3AF" textAnchor="middle">
          0
        </text>
        <text x="140" y="155" fontSize="12" fill="#9CA3AF" textAnchor="middle">
          100
        </text>
      </svg>
    </div>
  );
};

export default ReversedGaugeChart;
