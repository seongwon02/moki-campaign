import React from "react";
import type { Customer } from "../../types/customerTypes";
import { calculateLastVisitDate } from "../../utils/dateUtils";
import Button from "./Button";
import { useNavigate } from "react-router-dom";

interface CustomerListProps {
  customers: Customer[];
  visibleCount?: number;
  onShowMore?: () => void;
  baseDateString?: string;
}

const CustomerList: React.FC<CustomerListProps> = ({
  customers,
  visibleCount,
  onShowMore,
  baseDateString,
}) => {
  const customersToShow = visibleCount
    ? customers.slice(0, visibleCount)
    : customers;
  const showShowMoreButton =
    visibleCount && onShowMore && visibleCount < customers.length;
  const navigate = useNavigate();

  const handleCustomerClick = (customerId: number) => {
    navigate(`/customer-info/${customerId}`);
  };

  return (
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
          {customersToShow.map((customer) => (
            <tr
              key={customer.customer_id}
              onClick={() => handleCustomerClick(customer.customer_id)}
              className="cursor-pointer hover:bg-gray-50"
            >
              <td className="py-2 px-4 border-b text-sm text-gray-800">
                {customer.name}
              </td>
              <td className="py-2 px-4 border-b text-sm text-gray-800">
                {calculateLastVisitDate(customer.visit_day_ago, baseDateString)}
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
      {showShowMoreButton && (
        <Button
          variant="plain"
          className="w-full mt-4"
          onClick={onShowMore}
        >
          더보기
        </Button>
      )}
    </div>
  );
};

export default CustomerList;
