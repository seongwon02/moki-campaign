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

const RiskLoyal: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Title Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-xl font-bold text-black">
            방문 감소 충성 고객
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
      {/* Scrollable Content */}
      <div className="w-full max-w-md overflow-y-auto hide-scrollbar">
        {/* Section 1: 충성 고객 이탈 정보 표기 */}
        <div className="bg-white xl p-6 mb-0.5 flex space-x-4">
          <div className="w-1/2">
            {/* Left Section */}
            <p className="text-black font-bold text-base">
              충성 고객 중 방문 감소
            </p>
            <p className="text-[#4A7CE9] font-bold text-3xl">17명</p>
          </div>
          <div className="w-1/2">
            {/* Right Section */}
            <p className="text-black font-bold text-base">
              이탈 위험 충성 고객 비율
            </p>
            <p className="text-[#4A7CE9] font-bold text-3xl">24%</p>
          </div>
        </div>
        {/* Section 2: 이탈 위험 충성 고객 리스트 */}
        <div className="bg-white xl p-6 mb-0.5"></div>
      </div>
    </div>
  );
};

export default RiskLoyal;
