import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";

interface Customer {
  name: string;
  customer_id: number;
  visit_day_ago: number;
  total_visit_count: number;
  loyalty_score: number;
}

const CRM: React.FC = () => {
  const navigate = useNavigate();
  const [visibleCount, setVisibleCount] = useState(25);
  const [activeList, setActiveList] = useState<"all" | "loyal" | "churn">(
    "all"
  );

  const allCustomers: Customer[] = Array.from({ length: 50 }, (_, i) => ({
    name: `고객 ${i + 1}`,
    customer_id: i + 1,
    visit_day_ago: Math.floor(Math.random() * 200) + 1, // 1 to 200 days ago
    total_visit_count: Math.floor(Math.random() * 100) + 1, // 1 to 100 visits
    loyalty_score: Math.floor(Math.random() * 100), // 50 to 100 loyalty score
  }));

  const loyalCustomers: Customer[] = Array.from({ length: 50 }, (_, i) => ({
    name: `고객 ${i + 1}`,
    customer_id: i + 1,
    visit_day_ago: Math.floor(Math.random() * 60) + 1, // 1 to 60 days ago
    total_visit_count: Math.floor(Math.random() * 50) + 50, // 50 to 100 visits
    loyalty_score: Math.floor(Math.random() * 30) + 70, // 70 to 100 loyalty score
  }));

  const churnRiskCustomers: Customer[] = Array.from({ length: 50 }, (_, i) => ({
    name: `고객 ${i + 1}`,
    customer_id: i + 1,
    visit_day_ago: Math.floor(Math.random() * 140) + 60, // 60 to 200 days ago
    total_visit_count: Math.floor(Math.random() * 20) + 1, // 1 to 20 visits
    loyalty_score: Math.floor(Math.random() * 50), // 50 to 100 loyalty score
  }));

  const handleListChange = (list: "all" | "loyal" | "churn") => {
    setActiveList(list);
    setVisibleCount(25);
  };

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

  const handleShowMore = () => {
    setVisibleCount((prevCount) => prevCount + 25);
  };

  const currentCustomers =
    activeList === "all"
      ? allCustomers
      : activeList === "loyal"
      ? loyalCustomers
      : churnRiskCustomers;

  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Title Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-xl font-bold text-black">
            CRM
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
      <div className="w-full max-w-md overflow-y-auto hide-scrollbar">
        {/* Section 1: 고객 리스트 */}
        <div className="bg-white xl p-6 mb-0.5">
          <div className="flex mb-4 w-full border border-gray-300 rounded-lg overflow-hidden">
            <Button
              onClick={() => handleListChange("all")}
              variant={activeList === "all" ? "primary" : "secondary"}
              className="flex-grow rounded-l-lg rounded-r-none border-r border-gray-300"
            >
              전체 고객
            </Button>
            <Button
              onClick={() => handleListChange("loyal")}
              variant={activeList === "loyal" ? "primary" : "secondary"}
              className="flex-grow rounded-none border-r border-gray-300"
            >
              충성 고객
            </Button>
            <Button
              onClick={() => handleListChange("churn")}
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
                {currentCustomers
                  .slice(0, visibleCount)
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
          {visibleCount < currentCustomers.length && (
            <Button
              variant="plain"
              className="w-full mt-4"
              onClick={handleShowMore}
            >
              더보기
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default CRM;
