import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import type { CustomerDetail } from "../types/customerTypes";
import { calculateLastVisitDate, formatPhoneNumber } from "../utils/dateUtils";
import { getCustomerDetail } from "../services/crmApi"; // New import
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const CustomerInfo: React.FC = () => {
  const navigate = useNavigate();
  const { customerId } = useParams<{ customerId: string }>();
  const [customerData, setCustomerData] = useState<CustomerDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true); // New state
  const [error, setError] = useState<string | null>(null); // New state

  useEffect(() => {
    const fetchCustomerDetails = async () => {
      if (!customerId) {
        setError("고객 ID가 제공되지 않았습니다.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);
      try {
        const data = await getCustomerDetail(Number(customerId)); // Call the API
        setCustomerData(data);
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : "알 수 없는 오류가 발생했습니다.";
        setError(errorMessage);
        console.error("고객 상세 정보 불러오기 실패:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCustomerDetails();
  }, [customerId]);

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        display: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1,
        },
      },
    },
  };

  // Ensure customerData and analytics are available before mapping
  const chartData = {
    labels:
      customerData?.analytics?.map(
        // Added optional chaining
        (item) => `${parseInt(item.month.split("-")[1])}월`
      ) || [],
    datasets: [
      {
        label: "월별 방문 횟수",
        data: customerData?.analytics?.map((item) => item.count) || [], // Added optional chaining
        backgroundColor: "rgba(74, 124, 233, 0.6)",
        borderColor: "rgba(74, 124, 233, 1)",
        borderWidth: 1,
      },
    ],
  };

  if (!customerData) {
    return (
      <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
        <div className="w-full max-w-md">
          <div
            className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
            style={{ minHeight: "100px" }}
          >
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-xl font-bold text-black">
              고객 상세 정보
            </div>
            <Button
              onClick={() => navigate(-1)}
              variant="ghost"
              className="absolute bottom-3 left-1"
            >
              <img src={backIcon} alt="뒤로가기" className="w-2.5 h-5 mr-2" />
            </Button>
          </div>
        </div>
        <div className="w-full max_w-md overflow-y-auto hide-scrollbar p-4 text-center">
          로딩 중...
        </div>
      </div>
    );
  }

  const getChurnRiskLevelInKorean = (level: string) => {
    switch (level) {
      case "LOW":
        return "낮음";
      case "MEDIUM":
        return "보통";
      case "HIGH":
        return "높음";
      default:
        return level;
    }
  };

  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Title Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-xl font-bold text-black">
            고객 상세 정보
          </div>
          <Button
            onClick={() => navigate(-1)}
            variant="ghost"
            className="absolute bottom-3 left-1"
          >
            <img src={backIcon} alt="뒤로가기" className="w-2.5 h-5 mr-2" />
          </Button>
        </div>
      </div>
      {/* Contents Section (Scrollable) */}
      <div className="w-full max-w-md overflow-y-auto hide-scrollbar">
        {/* Section 1: 기본 정보 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-2">기본 정보</h3>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">이름:</span>
            <span className="font-bold flex items-center">
              {customerData.loyalty_score >= 70 && (
                <span className="mr-2 px-2 py-1 rounded-full text-sm bg-[#4A7CE9] text-white">
                  단골
                </span>
              )}
              {customerData.churn_risk_level === "HIGH" && (
                <span className="mr-2 px-2 py-1 rounded-full text-sm bg-red-500 text-white">
                  이탈 위험
                </span>
              )}
              {customerData.name}
            </span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">전화번호:</span>
            <span className="font-bold">{formatPhoneNumber(customerData.phone_number)}</span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">사용 금액:</span>
            <span className="font-bold">
              {customerData.total_spent.toLocaleString()}원
            </span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">단골 점수:</span>
            <span className="font-bold">{customerData.loyalty_score}점</span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">이탈 위험도:</span>
            <span className="font-bold">
              {getChurnRiskLevelInKorean(customerData.churn_risk_level)}
            </span>
          </div>
        </div>
        {/* Section 2: 포인트 관련 정보 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-2">포인트 정보</h3>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">현재 포인트:</span>
            <span className="font-bold">
              {customerData.current_points.toLocaleString()} P
            </span>
          </div>
        </div>
        {/* Section 3: 방문 횟수 정보 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-2">방문 정보</h3>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">총 방문 횟수:</span>
            <span className="font-bold">
              {customerData.total_visit_count}회
            </span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">마지막 방문일:</span>
            <span className="font-bold">
              {calculateLastVisitDate(customerData.visit_day_ago)}
            </span>
          </div>
        </div>
        {/* Section 4: 방문 빈도 그래프 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-4">최근 6개월 방문 빈도</h3>
          <Bar options={chartOptions} data={chartData} />
        </div>
      </div>
    </div>
  );
};

export default CustomerInfo;
