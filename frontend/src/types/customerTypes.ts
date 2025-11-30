export interface Customer {
  customer_id: number;
  name: string;
  visit_day_ago: number;
  total_visit_count: number;
  loyalty_score: number;
}

export interface CustomerDetail {
  customer_id: number;
  name: string;
  phone_number: string;
  total_spent: number;
  loyalty_score: number;
  churn_risk_level: string;
  segment: string;
  current_points: number;
  total_visit_count: number;
  visit_day_ago: number;
  analytics?: { month: string; count: number }[];
}

export interface Graph {
  label: string;
  count: number;
}