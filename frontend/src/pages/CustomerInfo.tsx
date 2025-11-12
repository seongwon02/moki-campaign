import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";

const CustomerInfo: React.FC = () => {
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
        <div className="bg-white xl p-6 mb-0.5"></div>
        {/* Section 2: 포인트 관련 정보 */}
        <div className="bg-white xl p-6 mb-0.5"></div>
        {/* Section 3: 방문 횟수 정보 */}
        <div className="bg-white xl p-6 mb-0.5"></div>
        {/* Section 4: 방문 빈도 그래프 */}
        <div className="bg-white xl p-6 mb-0.5"></div>
      </div>
    </div>
  );
};

export default CustomerInfo;
