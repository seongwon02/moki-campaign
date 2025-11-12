import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import type { Customer } from "../types/customerTypes";
import CustomerList from "../components/common/CustomerList";

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

          <CustomerList
            customers={currentCustomers}
            visibleCount={visibleCount}
            onShowMore={handleShowMore}
            baseDateString="2025-11-06T12:00:00Z"
          />
        </div>
      </div>
    </div>
  );
};

export default CRM;
