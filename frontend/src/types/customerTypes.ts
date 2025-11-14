export interface Customer {
  name: string;
  customer_id: number;
  visit_day_ago: number;
  total_visit_count: number;
  loyalty_score: number;
}

export interface CustomerDetail {
  name: string;
  visit: {
    total_visit_count: number;
    visit_day_ago: number;
  };
  analytics: {
    visit_frequency: Array<{
      month: string;
      count: number;
    }>;
  };
  customer_id: number;
  phone_number: string;
  total_spent: number;
  loyalty_score: number;
  churn_risk_level: "LOW" | "MEDIUM" | "HIGH"; // Assuming these are the possible values
  current_points: number;
}
