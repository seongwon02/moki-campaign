import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import backIcon from "../assets/icons/back.svg";
import type { Customer } from "../types/customerTypes";
import CustomerList from "../components/common/CustomerList";
import { getCustomers, type CustomerType } from "../services/crmApi";

const CRM: React.FC = () => {
  const navigate = useNavigate();
  const [activeList, setActiveList] = useState<"all" | "loyal" | "churn">(
    "all"
  );
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCustomers = async () => {
      setLoading(true);
      setError(null);
      // Reset customers and page when the list type changes
      setCustomers([]);
      setPage(0);

      const segment: CustomerType =
        activeList === "churn" ? "churn_risk" : activeList;

      try {
        const data = await getCustomers({
          segment,
          page: 0,
          size: 25,
        });
        setCustomers(data.customers);
        setHasNext(data.has_next);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "An unknown error occurred.";
        setError(errorMessage);
        console.error(`Failed to fetch ${activeList} customers:`, err);
      } finally {
        setLoading(false);
      }
    };

    fetchCustomers();
  }, [activeList]);

  const handleShowMore = async () => {
    if (!hasNext) return;

    const nextPage = page + 1;
    const segment: CustomerType =
      activeList === "churn" ? "churn_risk" : activeList;

    try {
      const data = await getCustomers({
        segment,
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

  const handleListChange = (list: "all" | "loyal" | "churn") => {
    setActiveList(list);
  };

  const today = new Date();
  const baseDateString = today.toISOString();

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

          {loading ? (
            <div className="text-center p-4">Loading...</div>
          ) : error && customers.length === 0 ? (
            <div className="text-center p-4 text-red-500">Error: {error}</div>
          ) : (
            <CustomerList
              customers={customers}
              visibleCount={customers.length}
              onShowMore={() => {}}
              baseDateString={baseDateString}
            />
          )}
        </div>
        {error && customers.length > 0 && (
          <p className="text-red-500 text-center p-2">{error}</p>
        )}
        {hasNext && !loading && (
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

export default CRM;
