import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import type { CustomerDetail } from "../types/customerTypes";
import { calculateLastVisitDate } from "../utils/dateUtils";

const CustomerInfo: React.FC = () => {
  const navigate = useNavigate();
  const { customerId } = useParams<{ customerId: string }>();
  const [customerData, setCustomerData] = useState<CustomerDetail | null>(null);

  useEffect(() => {
    const fetchCustomerDetails = async () => {
      // Simulate API call
      const mockData: CustomerDetail = {
        name: "홍길동",
        visit: {
          total_visit_count: 23,
          visit_day_ago: 5,
        },
        analytics: {
          visit_frequency: [
            {
              month: "2025-09",
              count: 5,
            },
          ],
        },
        customer_id: 1,
        phone_number: "010-1234-5678",
        total_spent: 500000,
        loyalty_score: 85,
        churn_risk_level: "LOW",
        current_points: 1500,
      };
      // In a real application, we would fetch data from an API:
      // const response = await fetch(`/api/customers/${customerId}`);
      // const data = await response.json();
      setCustomerData(mockData);
    };

    if (customerId) {
      fetchCustomerDetails();
    }
  }, [customerId]);

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
        <div className="w-full max-w-md overflow-y-auto hide-scrollbar p-4 text-center">
          로딩 중...
        </div>
      </div>
    );
  }

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
          <p>
            <strong>이름:</strong> {customerData.name}
          </p>
          <p>
            <strong>전화번호:</strong> {customerData.phone_number}
          </p>
          <p>
            <strong>사용 금액:</strong>{" "}
            {customerData.total_spent.toLocaleString()}원
          </p>
          <p>
            <strong>이탈 위험도:</strong> {customerData.churn_risk_level}
          </p>
        </div>
        {/* Section 2: 포인트 관련 정보 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-2">포인트 정보</h3>
          <p>
            <strong>현재 포인트:</strong>{" "}
            {customerData.current_points.toLocaleString()}점
          </p>
        </div>
        {/* Section 3: 방문 횟수 정보 */}
        <div className="bg-white xl p-6 mb-0.5">
          <h3 className="text-lg font-semibold mb-2">방문 정보</h3>
          <p>
            <strong>총 방문 횟수:</strong>{" "}
            {customerData.visit.total_visit_count}회
          </p>
          <p>
            <strong>마지막 방문일:</strong>{" "}
            {calculateLastVisitDate(customerData.visit.visit_day_ago)}
          </p>
        </div>
        {/* Section 4: 방문 빈도 그래프 */}
        <div className="bg-white xl p-6 mb-0.5 flex space-x-4"></div>
      </div>
    </div>
  );
};

export default CustomerInfo;
