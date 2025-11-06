import React from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import { logout } from "../store/authSlice";
import { type AppDispatch } from "../store";

const MainDashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  return (
    <div className="h-screen bg-gray-50 flex flex-col items-center p-4">
      {/* Store Name Section (Fixed) */}
      <div className="w-full max-w-md">
        <div
          className="bg-white rounded-xl p-6 mb-4 flex items-end"
          style={{ minHeight: "100px" }}
        >
          <div className="text-center text-sm text-gray-500 w-full">
            [가게 이름]
          </div>
        </div>
      </div>

      {/* Scrollable Content */}
      <div className="w-full max-w-md overflow-y-auto">
        <div className="bg-white rounded-xl p-6">
          {/* Section 1: This Week's Information Summary */}
          <div>
            <h2 className="text-xl font-bold text-indigo-700 mb-4">
              이번주 정보 요약
            </h2>
            <p className="text-gray-600">
              이번주 정보 요약 내용이 여기에 표시됩니다.
            </p>
          </div>

          <hr className="my-6 border-gray-200" />

          {/* Section 2: Decreased Visit Customer Management */}
          <div>
            <h2 className="text-xl font-bold text-indigo-700 mb-4">
              방문 감소 고객 관리
            </h2>
            <p className="text-gray-600">
              방문 감소 고객 관리 내용이 여기에 표시됩니다.
            </p>
          </div>

          <hr className="my-6 border-gray-200" />

          {/* Section 3: CRM */}
          <div>
            <h2 className="text-xl font-bold text-indigo-700 mb-4">CRM</h2>
            <p className="text-gray-600">CRM 관련 내용이 여기에 표시됩니다.</p>
          </div>
        </div>

        {/* Logout Button */}
        <div className="mt-8 text-center">
          <Button onClick={handleLogout} variant="primary">
            로그아웃
          </Button>
        </div>
      </div>
    </div>
  );
};

export default MainDashboard;
