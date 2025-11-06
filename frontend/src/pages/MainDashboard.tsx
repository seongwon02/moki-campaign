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
          <h2 className="text-xl font-bold text-indigo-700 mb-4">
            이번주 정보 요약
          </h2>
          <p className="text-gray-600">
            이번주 정보 요약 내용이 여기에 표시됩니다.
          </p>
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
