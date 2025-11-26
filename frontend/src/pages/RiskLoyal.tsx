import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import churnIconGreen from "../assets/icons/churn_green.svg";
import churnIconYellow from "../assets/icons/churn_yellow.svg";
import churnIconRed from "../assets/icons/churn_red.svg";
import type { Customer } from "../types/customerTypes";
import CustomerList from "../components/common/CustomerList";
import { getDeclineCustomers } from "../services/atRiskLoyalApi";
import { getCustomers } from "../services/crmApi";
import ReversedGaugeChart from "../components/common/ReversedGaugeChart";

// NOTE: The structure of the response from getDeclineCustomers is assumed here.
interface DeclineStats {
  decline_customer_count: number;
  decline_customer_rate: number;
}

const RiskLoyal: React.FC = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [stats, setStats] = useState<DeclineStats | null>(null);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchInitialData = async () => {
      setLoading(true);
      setError(null);
      try {
        const declineData = await getDeclineCustomers();
        setStats(declineData);

        const customerData = await getCustomers({
          segment: "at_risk_loyal",
          page: 0,
          size: 25,
        });
        setCustomers(customerData.customers);
        setHasNext(customerData.has_next);
        setPage(0);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "An unknown error occurred.";
        setError(errorMessage);
        console.error("Failed to fetch initial data:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchInitialData();
  }, []);

  const handleShowMore = async () => {
    if (!hasNext) return;

    const nextPage = page + 1;
    try {
      const data = await getCustomers({
        segment: "at_risk_loyal",
        page: nextPage,
        size: 25,
      });
      setCustomers((prev) => [...prev, ...data.customers]);
      setHasNext(data.has_next);
      setPage(nextPage);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "An unknown error occurred.";
      setError(errorMessage);
      console.error("Failed to fetch more customers:", err);
    }
  };

  const declineRate = stats?.decline_customer_rate ?? 0;

  const getChurnIndicatorColor = (rate: number): string => {
    if (rate <= 50) {
      return "text-[#34D399]"; // Green (matches chart's green)
    } else if (rate <= 75) {
      return "text-[#FBBF24]"; // Yellow (matches chart's yellow)
    } else {
      return "text-[#F87171]"; // Red (matches chart's red)
    }
  };

  const indicatorColor = getChurnIndicatorColor(declineRate);

  const getChurnIconColor = (rate: number): string => {
    if (rate <= 50) {
      return churnIconGreen;
    } else if (rate <= 75) {
      return churnIconYellow;
    } else {
      return churnIconRed;
    }
  };

  const churnIconColor = getChurnIconColor(declineRate);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  if (error && customers.length === 0) {
    return (
      <div className="flex justify-center items-center h-screen">
        Error: {error}
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
            방문 감소 단골 고객
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
        {/* Section 1: 단골 고객 이탈 정보 표기 */}
        <div className="bg-white xl p-6 mb-0.5 flex justify-between items-center">
          {/* Left Half: Churn Count */}
          <div className="w-1/2 flex flex-col items-center justify-center h-full">
            <div className="flex items-center mb-2">
              <img
                src={churnIconColor}
                alt="이탈 아이콘"
                className="w-6 h-6 mr-2"
              />
              <p className="text-lg font-semibold">이탈 위험 고객</p>
            </div>
            <p className="text-4xl font-bold">
              <span className={indicatorColor}>
                {stats?.decline_customer_count ?? 0}
              </span>
              명
            </p>
          </div>
          {/* Right Half: Chart */}
          <div className="w-1/2 flex flex-col items-center justify-center">
            <ReversedGaugeChart value={declineRate} />
          </div>
        </div>
        {/* Section 2: 이탈 위험 단골 고객 리스트 */}
        <div className="bg-white xl p-6 mb-0.5">
          <CustomerList
            customers={customers}
            visibleCount={customers.length}
            onShowMore={() => {}}
            baseDateString="2025-11-06T12:00:00Z"
          />
        </div>
        {error && <p className="text-red-500 text-center p-2">{error}</p>}
        {hasNext && (
          <div className="bg-white p-4 mt-0.5">
            <Button
              onClick={handleShowMore}
              className="w-full"
              variant="secondary"
            >
              더보기
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default RiskLoyal;
