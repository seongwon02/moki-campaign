import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import type { CustomerDetail, Graph } from "../types/customerTypes";
import {
  calculateLastVisitDate,
  formatPhoneNumber,
  formatChartLabel,
} from "../utils/dateUtils";
import { getCustomerDetail, getCustomerDetailGraph } from "../services/crmApi";
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
  const [customerGraphData, setCustomerGraphData] = useState<Graph[] | null>(
    null
  );
  const [period, setPeriod] = useState<"month" | "week">("week");

  useEffect(() => {
    const fetchCustomerDetails = async () => {
      if (!customerId) {
        console.error("고객 ID가 제공되지 않았습니다.");
        return;
      }

      try {
        const data = await getCustomerDetail(Number(customerId));
        setCustomerData(data);
      } catch (err) {
        console.error("고객 상세 정보 불러오기 실패:", err);
      }
    };

    fetchCustomerDetails();
  }, [customerId]);

  useEffect(() => {
    const fetchGraphData = async () => {
      if (!customerId) return;

      try {
        const graphData = await getCustomerDetailGraph(
          Number(customerId),
          period
        );
        setCustomerGraphData(graphData);
      } catch (error) {
        console.error("그래프 데이터 로딩 실패:", error);
        setCustomerGraphData([]); // Clear previous data on error
      }
    };

    fetchGraphData();
  }, [customerId, period]);

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

  const chartData = {
    labels:
      customerGraphData?.map((item, index, array) => {
        if (period === "week") {
          if (index == 7) {
            return `이번주`;
          } else {
            return `${array.length - index - 1}주전`;
          }
        }
        // Assuming period is 'month' and item.label is 'yyyy-MM'
        return formatChartLabel(item.label);
      }) || [],
    datasets: [
      {
        label: period === "week" ? "주별 방문 횟수" : "월별 방문 횟수",
        data: customerGraphData?.map((item) => item.count) || [],
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
              {(customerData.segment === "LOYAL" ||
                customerData.segment === "AT_RISK_LOYAL") && (
                <span className="mr-2 px-2 py-1 rounded-full text-sm bg-[#4A7CE9] text-white">
                  단골
                </span>
              )}
              {(customerData.churn_risk_level === "HIGH" ||
                customerData.segment === "AT_RISK_LOYAL") && (
                <span className="mr-2 px-2 py-1 rounded-full text-sm bg-red-500 text-white">
                  이탈 위험
                </span>
              )}
              {customerData.name}
            </span>
          </div>
          <div className="flex justify-between mb-2 text-xl">
            <span className="font-semibold">전화번호:</span>
            <span className="font-bold">
              {formatPhoneNumber(customerData.phone_number)}
            </span>
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
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold">
              {period === "week"
                ? "최근 8주 방문 빈도"
                : "최근 6개월 방문 빈도"}
            </h3>
            <div className="flex space-x-2">
              <Button
                onClick={() => setPeriod("week")}
                variant={period === "week" ? "primary" : "ghost"}
              >
                주별
              </Button>
              <Button
                onClick={() => setPeriod("month")}
                variant={period === "month" ? "primary" : "ghost"}
              >
                월별
              </Button>
            </div>
          </div>
          {customerGraphData ? (
            <Bar options={chartOptions} data={chartData} />
          ) : (
            <div>그래프 데이터를 불러오는 중입니다...</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CustomerInfo;
