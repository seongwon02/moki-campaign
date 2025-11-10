import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import { type AppDispatch } from "../store";
import SettingsPage from "./Settings.tsx";
import moneyIcon from "../assets/icons/money.svg";
import groupIcon from "../assets/icons/group.svg";
import revisitIcon from "../assets/icons/revisit.svg";
import settingIcon from "../assets/icons/setting.svg";

interface Customer {
  name: string;
  customer_id: number;
  visit_day_ago: number;
  total_visit_count: number;
  loyalty_score: number;
}

const MainDashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const [showSettings, setShowSettings] = useState(false);
  const [activeList, setActiveList] = useState<"all" | "loyal" | "churn">(
    "all"
  );

  const getWeekRange = () => {
    const today = new Date(); // Using fixed date from user context
    const dayOfWeek = today.getDay(); // Sunday = 0, Monday = 1, ..., Saturday = 6
    const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    const monday = new Date(today);
    monday.setDate(today.getDate() + mondayOffset);

    const formatDate = (date: Date) => {
      return `${date.getMonth() + 1}.${date.getDate()}`;
    };

    return `${formatDate(monday)} ~ ${formatDate(today)}`;
  };

  // Sample data for the table (to be replaced with API data)
  const loyalCustomers: Customer[] = [
    {
      name: "김철수",
      customer_id: 1,
      visit_day_ago: 5,
      total_visit_count: 15,
      loyalty_score: 85,
    },
    {
      name: "이영희",
      customer_id: 2,
      visit_day_ago: 8,
      total_visit_count: 12,
      loyalty_score: 78,
    },
    {
      name: "박민수",
      customer_id: 3,
      visit_day_ago: 13,
      total_visit_count: 10,
      loyalty_score: 70,
    },
    {
      name: "최지영",
      customer_id: 4,
      visit_day_ago: 15,
      total_visit_count: 8,
      loyalty_score: 65,
    },
    {
      name: "정수진",
      customer_id: 5,
      visit_day_ago: 18,
      total_visit_count: 7,
      loyalty_score: 60,
    },
    {
      name: "강현우",
      customer_id: 9,
      visit_day_ago: 2,
      total_visit_count: 20,
      loyalty_score: 90,
    },
    {
      name: "윤서연",
      customer_id: 10,
      visit_day_ago: 7,
      total_visit_count: 14,
      loyalty_score: 82,
    },
  ];

  // Sample data for Section 3
  const allCustomers: Customer[] = [
    {
      name: "김철수",
      customer_id: 1,
      visit_day_ago: 5,
      total_visit_count: 15,
      loyalty_score: 85,
    },
    {
      name: "이영희",
      customer_id: 2,
      visit_day_ago: 8,
      total_visit_count: 12,
      loyalty_score: 78,
    },
    {
      name: "박민수",
      customer_id: 3,
      visit_day_ago: 13,
      total_visit_count: 10,
      loyalty_score: 70,
    },
    {
      name: "최지영",
      customer_id: 4,
      visit_day_ago: 15,
      total_visit_count: 8,
      loyalty_score: 65,
    },
    {
      name: "정수진",
      customer_id: 5,
      visit_day_ago: 18,
      total_visit_count: 7,
      loyalty_score: 60,
    },
    {
      name: "홍길동",
      customer_id: 6,
      visit_day_ago: 30,
      total_visit_count: 2,
      loyalty_score: 30,
    },
    {
      name: "김민준",
      customer_id: 7,
      visit_day_ago: 45,
      total_visit_count: 1,
      loyalty_score: 10,
    },
    {
      name: "오지현",
      customer_id: 8,
      visit_day_ago: 25,
      total_visit_count: 3,
      loyalty_score: 40,
    },
    {
      name: "강현우",
      customer_id: 9,
      visit_day_ago: 2,
      total_visit_count: 20,
      loyalty_score: 90,
    },
    {
      name: "윤서연",
      customer_id: 10,
      visit_day_ago: 7,
      total_visit_count: 14,
      loyalty_score: 82,
    },
    {
      name: "이하준",
      customer_id: 11,
      visit_day_ago: 60,
      total_visit_count: 1,
      loyalty_score: 5,
    },
    {
      name: "박하은",
      customer_id: 12,
      visit_day_ago: 20,
      total_visit_count: 6,
      loyalty_score: 55,
    },
  ];

  const churnRiskCustomers: Customer[] = [
    {
      name: "홍길동",
      customer_id: 6,
      visit_day_ago: 30,
      total_visit_count: 2,
      loyalty_score: 30,
    },
    {
      name: "김민준",
      customer_id: 7,
      visit_day_ago: 45,
      total_visit_count: 1,
      loyalty_score: 10,
    },
    {
      name: "오지현",
      customer_id: 8,
      visit_day_ago: 25,
      total_visit_count: 3,
      loyalty_score: 40,
    },
    {
      name: "이하준",
      customer_id: 11,
      visit_day_ago: 60,
      total_visit_count: 1,
      loyalty_score: 5,
    },
    {
      name: "최은지",
      customer_id: 13,
      visit_day_ago: 35,
      total_visit_count: 2,
      loyalty_score: 25,
    },
    {
      name: "정우진",
      customer_id: 14,
      visit_day_ago: 50,
      total_visit_count: 1,
      loyalty_score: 8,
    },
  ];

  const calculateLastVisitDate = (visitDayAgo: number) => {
    const today = new Date("2025-11-06T12:00:00Z"); // Fixed date from user context
    const lastVisitDate = new Date(today);
    lastVisitDate.setDate(today.getDate() - visitDayAgo);
    return `${lastVisitDate.getFullYear()}.${(lastVisitDate.getMonth() + 1)
      .toString()
      .padStart(2, "0")}.${lastVisitDate
      .getDate()
      .toString()
      .padStart(2, "0")}`;
  };

  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Store Name Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-sm text-gray-500">
            [가게 이름]
          </div>
          <Button
            onClick={() => setShowSettings(true)}
            variant="ghost"
            className="absolute bottom-2 right-1"
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
            <span className="text-sm text-gray-500">{getWeekRange()}</span>
          </div>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <img
                  src={moneyIcon}
                  alt="주간 매출"
                  className="w-10 h-10 mr-2"
                />
                <span className="font-bold text-xl text-black">주간 매출</span>
              </div>
              <div className="text-right">
                <span className="font-bold text-lg text-gray-800">
                  1,234,567원
                </span>
                <p className="text-sm text-red-500">
                  <span className="text-gray-500">저번주 대비 </span>▲ 123,456원
                </p>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <img
                  src={groupIcon}
                  alt="방문자 수"
                  className="w-10 h-10 mr-2"
                />
                <span className="font-bold text-xl text-black">방문자 수</span>
              </div>
              <div className="text-right">
                <span className="font-bold text-lg text-gray-800">123명</span>
                <p className="text-sm text-[#4A7CE9]">
                  <span className="text-gray-500">저번주 대비 </span>▼ 12명
                </p>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
                <img
                  src={revisitIcon}
                  alt="재방문율"
                  className="w-10 h-10 mr-2"
                />
                <span className="font-bold text-xl text-black">재방문율</span>
              </div>
              <div className="text-right">
                <span className="font-bold text-lg text-gray-800">45%</span>
                <p className="text-sm text-red-500">
                  <span className="text-gray-500">저번주 대비 </span>▲ 5%
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Section 2: Decreased Visit Customer Management */}
        <div className="bg-white xl p-6 mb-0.5">
          <h2 className="text-lg font-bold text-black mb-4">
            방문 감소 충성 고객
          </h2>
          <p className="text-xs text-gray-700 mb-2">
            단골 고객 중 <span className="text-[#4A7CE9]">17명</span>의 방문
            횟수가 감소하고 있습니다
          </p>
          <p className="text-xs text-gray-700 mb-4">
            현재 단골 고객 중 이탈 위험 고객의 비율은{" "}
            <span className="text-[#4A7CE9]">24%</span>입니다
          </p>
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white">
              <thead>
                <tr>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    이름
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    최근 방문
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    방문 횟수
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    단골 점수
                  </th>
                </tr>
              </thead>
              <tbody>
                {loyalCustomers.slice(0, 5).map((customer) => (
                  <tr key={customer.customer_id}>
                    <td className="py-2 px-4 border-b text-sm text-gray-800">
                      {customer.name}
                    </td>
                    <td className="py-2 px-4 border-b text-sm text-gray-800">
                      {calculateLastVisitDate(customer.visit_day_ago)}
                    </td>
                    <td className="py-2 px-4 border-b text-sm text-gray-800">
                      {customer.total_visit_count}회
                    </td>
                    <td className="py-2 px-4 border-b text-sm text-gray-800">
                      {customer.loyalty_score}점
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {/* View Details Button for Section 2 */}
          <Button variant="primary" className="w-full mt-4" onClick={() => navigate('/risk-loyal')}>
            자세히 보기
          </Button>
        </div>

        {/* Section 3: CRM */}
        <div className="bg-white xl p-6">
          <h2 className="text-lg font-bold text-black mb-4">CRM</h2>
          <div className="flex mb-4 w-full border border-gray-300 rounded-lg overflow-hidden">
            <Button
              onClick={() => setActiveList("all")}
              variant={activeList === "all" ? "primary" : "secondary"}
              className="flex-grow rounded-l-lg rounded-r-none border-r border-gray-300"
            >
              전체 고객
            </Button>
            <Button
              onClick={() => setActiveList("loyal")}
              variant={activeList === "loyal" ? "primary" : "secondary"}
              className="flex-grow rounded-none border-r border-gray-300"
            >
              충성 고객
            </Button>
            <Button
              onClick={() => setActiveList("churn")}
              variant={activeList === "churn" ? "primary" : "secondary"}
              className="flex-grow rounded-r-lg rounded-l-none"
            >
              이탈 위험
            </Button>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full bg-white">
              <thead>
                <tr>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    이름
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    최근 방문
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    방문 횟수
                  </th>
                  <th className="py-2 px-4 border-b text-left text-sm font-semibold text-[#4A7CE9]">
                    단골 점수
                  </th>
                </tr>
              </thead>
              <tbody>
                {(activeList === "all"
                  ? allCustomers
                  : activeList === "loyal"
                  ? loyalCustomers
                  : churnRiskCustomers
                )
                  .slice(0, 5)
                  .map((customer) => (
                    <tr key={customer.customer_id}>
                      <td className="py-2 px-4 border-b text-sm text-gray-800">
                        {customer.name}
                      </td>
                      <td className="py-2 px-4 border-b text-sm text-gray-800">
                        {calculateLastVisitDate(customer.visit_day_ago)}
                      </td>
                      <td className="py-2 px-4 border-b text-sm text-gray-800">
                        {customer.total_visit_count}회
                      </td>
                      <td className="py-2 px-4 border-b text-sm text-gray-800">
                        {customer.loyalty_score}점
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
          {/* View Details Button for Section 3 */}
          <Button variant="primary" className="w-full mt-4">
            자세히 보기
          </Button>
        </div>
      </div>
      {showSettings && <SettingsPage onClose={() => setShowSettings(false)} />}
    </div>
  );
};

export default MainDashboard;
