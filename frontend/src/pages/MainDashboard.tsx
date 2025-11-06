import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import { type AppDispatch } from "../store";
import SettingsPage from "./Settings.tsx";

const MainDashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const [showSettings, setShowSettings] = useState(false);

  const getWeekRange = () => {
    const today = new Date('2025-11-06T12:00:00Z'); // Using fixed date from user context
    const dayOfWeek = today.getDay(); // Sunday = 0, Monday = 1, ..., Saturday = 6
    const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    const monday = new Date(today);
    monday.setDate(today.getDate() + mondayOffset);

    const formatDate = (date: Date) => {
      return `${date.getMonth() + 1}.${date.getDate()}`;
    };

    return `${formatDate(monday)} ~ ${formatDate(today)}`;
  };

  return (
    <div className="h-screen bg-[#F2F3F7] flex flex-col items-center p-4">
      {/* Store Name Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white rounded-xl p-6 mb-0.5 relative flex items-end justify-end"
          style={{ minHeight: "100px" }}
        >
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-sm text-gray-500">
            [가게 이름]
          </div>
          <Button
            onClick={() => setShowSettings(true)}
            variant="ghost"
            className="p-2"
          >
            설정
          </Button>
        </div>
      </div>

      {/* Scrollable Content */}
      <div className="w-full max-w-md overflow-y-auto">
        {/* Section 1: This Week's Information Summary */}
        <div className="bg-white rounded-xl p-6 mb-0.5">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-indigo-700">
              이번주 정보 요약
            </h2>
            <span className="text-sm text-gray-500">
              {getWeekRange()}
            </span>
          </div>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">주간 매출</span>
              <div className="text-right">
                <span className="font-bold text-lg text-gray-800">1,234,567원</span>
                <p className="text-sm text-red-500">
                  <span className="text-gray-500">저번주 대비 </span>▲ 123,456원
                </p>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">방문자 수</span>
              <div className="text-right">
                <span className="font-bold text-lg text-gray-800">123명</span>
                <p className="text-sm text-blue-500">
                  <span className="text-gray-500">저번주 대비 </span>▼ 12명
                </p>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">재방문율</span>
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
        <div className="bg-white rounded-xl p-6 mb-0.5">
          <h2 className="text-xl font-bold text-indigo-700 mb-4">
            방문 감소 고객 관리
          </h2>
          <p className="text-gray-600">
            방문 감소 고객 관리 내용이 여기에 표시됩니다.
          </p>
        </div>

        {/* Section 3: CRM */}
        <div className="bg-white rounded-xl p-6">
          <h2 className="text-xl font-bold text-indigo-700 mb-4">CRM</h2>
          <p className="text-gray-600">CRM 관련 내용이 여기에 표시됩니다.</p>
        </div>
      </div>
      {showSettings && <SettingsPage onClose={() => setShowSettings(false)} />}
    </div>
  );
};

export default MainDashboard;
