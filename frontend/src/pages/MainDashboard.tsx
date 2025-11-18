import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import SettingsPage from "./Settings.tsx";
import moneyIcon from "../assets/icons/money.svg";
import groupIcon from "../assets/icons/group.svg";
import revisitIcon from "../assets/icons/revisit.svg";
import settingIcon from "../assets/icons/setting.svg";
import type { Customer } from "../types/customerTypes.ts";
import CustomerList from "../components/common/CustomerList.tsx";
import { getCustomers } from "../services/crmApi.ts";
import { getWeeklySummary } from "../services/weeklySummaryApi.ts";
import { getDeclineCustomers } from "../services/atRiskLoyalApi.ts";

// API 응답 데이터 타입 정의
interface WeeklySummaryData {
  store_name: string;
  start_date: string;
  end_date: string;
  total_sales: number;
  sales_change: number;
  visited_customer_count: number;
  customer_count_change: number;
  revisit_rate: number;
  revisit_rate_change: number;
}

interface AtRiskLoyalData {
  decline_count: number;
  decline_ratio: number;
}

const MainDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [showSettings, setShowSettings] = useState(false);
  const [activeList, setActiveList] = useState<"all" | "loyal" | "churn">(
    "all"
  );

  // 주간 요약 정보 상태
  const [summaryData, setSummaryData] = useState<WeeklySummaryData | null>(
    null
  );
  const [atRiskData, setAtRiskData] = useState<AtRiskLoyalData | null>(null);
  const [atRiskLoyalCustomers, setAtRiskLoyalCustomers] = useState<Customer[]>(
    []
  );
  const [crmCustomers, setCrmCustomers] = useState<Customer[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCrmData = async (segment: "all" | "loyal" | "churn_risk") => {
    try {
      const response = await getCustomers({ segment, size: 25, page: 0 });
      setCrmCustomers(response.customers);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("An unknown error occurred.");
      }
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const token = localStorage.getItem("authToken");
        if (!token) {
          throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
        }

        const [summary, atRisk, atRiskLoyalResponse] = await Promise.all([
          getWeeklySummary(),
          getDeclineCustomers(),
          getCustomers({ segment: "at_risk_loyal", size: 5, page: 0 }),
        ]);

        setSummaryData(summary);
        setAtRiskData(atRisk);
        setAtRiskLoyalCustomers(atRiskLoyalResponse.customers);

        // 초기 CRM 데이터 로드
        await fetchCrmData("all");
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("An unknown error occurred.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleActiveListChange = (listType: "all" | "loyal" | "churn") => {
    setActiveList(listType);
    let segment: "all" | "loyal" | "churn_risk" = "all";
    if (listType === "loyal") {
      segment = "loyal";
    } else if (listType === "churn") {
      segment = "churn_risk";
    }
    fetchCrmData(segment);
  };

  // YYYY-MM-DD 형식의 날짜를 MM.DD로 변환하는 함수
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}.${date.getDate()}`;
  };

  // 숫자 변화량을 포맷하는 헬퍼 함수
  const formatChange = (value: number, unit: string) => {
    const isPositive = value > 0;
    const isNegative = value < 0;
    const symbol = isPositive ? "▲" : isNegative ? "▼" : "";
    const colorClass = isPositive
      ? "text-red-500"
      : isNegative
      ? "text-[#4A7CE9]"
      : "text-gray-500";

    return (
      <p className={`text-sm ${colorClass}`}>
        <span className="text-gray-500">저번주 대비 </span>
        {symbol} {Math.abs(value).toLocaleString()}
        {unit}
      </p>
    );
  };

  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Store Name Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 font-bold text-lg text-black">
            {summaryData ? summaryData.store_name : "[가게 이름]"}
          </div>
          <Button
            onClick={() => setShowSettings(true)}
            variant="ghost"
            className="absolute bottom-4 right-1"
          >
            <img src={settingIcon} alt="설정" className="w-4 h-4 mr-2" />
          </Button>
        </div>
      </div>

      {/* Scrollable Content */}
      <div className="w-full max-w-md overflow-y-auto hide-scrollbar">
        {/* Section 1: This Week's Information Summary */}
        <div className="bg-white xl p-6 mb-0.5">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-black">이번주 정보 요약</h2>
            {summaryData && (
              <span className="text-sm text-gray-500">
                {formatDate(summaryData.start_date)} ~{" "}
                {formatDate(summaryData.end_date)}
              </span>
            )}
          </div>
          {isLoading ? (
            <div className="text-center py-10">
              <p>데이터를 불러오는 중...</p>
            </div>
          ) : error ? (
            <div className="text-center py-10 text-red-500">
              <p>오류: {error}</p>
            </div>
          ) : (
            summaryData && (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <div className="flex items-center">
                    <img
                      src={moneyIcon}
                      alt="주간 매출"
                      className="w-10 h-10 mr-2"
                    />
                    <span className="font-bold text-xl text-black">
                      주간 매출
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-lg text-gray-800">
                      {summaryData.total_sales.toLocaleString()}원
                    </span>
                    {formatChange(summaryData.sales_change, "원")}
                  </div>
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex items-center">
                    <img
                      src={groupIcon}
                      alt="방문자 수"
                      className="w-10 h-10 mr-2"
                    />
                    <span className="font-bold text-xl text-black">
                      방문자 수
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-lg text-gray-800">
                      {summaryData.visited_customer_count.toLocaleString()}명
                    </span>
                    {formatChange(summaryData.customer_count_change, "명")}
                  </div>
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex items-center">
                    <img
                      src={revisitIcon}
                      alt="재방문율"
                      className="w-10 h-10 mr-2"
                    />
                    <span className="font-bold text-xl text-black">
                      재방문율
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="font-bold text-lg text-gray-800">
                      {summaryData.revisit_rate}%
                    </span>
                    {formatChange(summaryData.revisit_rate_change, "%")}
                  </div>
                </div>
              </div>
            )
          )}
        </div>

        {/* Section 2: Decreased Visit Customer Management */}
        <div className="bg-white xl p-6 mb-0.5">
          <h2 className="text-lg font-bold text-black mb-4">
            방문 감소 충성 고객
          </h2>
          {isLoading ? (
            <p className="text-xs text-gray-700 mb-4">
              데이터를 불러오는 중...
            </p>
          ) : error ? (
            <p className="text-xs text-red-500 mb-4">
              데이터 로딩 중 오류가 발생했습니다.
            </p>
          ) : (
            atRiskData && (
              <>
                <p className="text-xs text-gray-700 mb-2">
                  단골 고객 중{" "}
                  <span className="text-[#4A7CE9]">
                    {atRiskData.decline_count}명
                  </span>
                  의 방문 횟수가 감소하고 있습니다
                </p>
                <p className="text-xs text-gray-700 mb-4">
                  현재 단골 고객 중 이탈 위험 고객의 비율은{" "}
                  <span className="text-[#4A7CE9]">
                    {atRiskData.decline_ratio}%
                  </span>
                  입니다
                </p>
              </>
            )
          )}
          <CustomerList customers={atRiskLoyalCustomers.slice(0, 5)} />
          {/* View Details Button for Section 2 */}
          <Button
            variant="primary"
            className="w-full mt-4"
            onClick={() => navigate("/risk-loyal")}
          >
            자세히 보기
          </Button>
        </div>

        {/* Section 3: CRM */}
        <div className="bg-white xl p-6">
          <h2 className="text-lg font-bold text-black mb-4">CRM</h2>
          <div className="flex mb-4 w-full border border-gray-300 rounded-lg overflow-hidden">
            <Button
              onClick={() => handleActiveListChange("all")}
              variant={activeList === "all" ? "primary" : "secondary"}
              className="flex-grow rounded-l-lg rounded-r-none border-r border-gray-300"
            >
              전체 고객
            </Button>
            <Button
              onClick={() => handleActiveListChange("loyal")}
              variant={activeList === "loyal" ? "primary" : "secondary"}
              className="flex-grow rounded-none border-r border-gray-300"
            >
              충성 고객
            </Button>
            <Button
              onClick={() => handleActiveListChange("churn")}
              variant={activeList === "churn" ? "primary" : "secondary"}
              className="flex-grow rounded-r-lg rounded-l-none"
            >
              이탈 위험
            </Button>
          </div>

          <CustomerList customers={crmCustomers.slice(0, 5)} />
          {/* View Details Button for Section 3 */}
          <Button
            variant="primary"
            className="w-full mt-4"
            onClick={() => navigate("/crm")}
          >
            자세히 보기
          </Button>
        </div>
      </div>
      {showSettings && <SettingsPage onClose={() => setShowSettings(false)} />}
    </div>
  );
};

export default MainDashboard;
