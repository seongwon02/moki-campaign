import axios, { type AxiosInstance } from "axios";
import { type Customer, type CustomerDetail, type Analytics } from "../types/customerTypes.ts";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000, // 10초 타임아웃
});

export type CustomerType = "all" | "loyal" | "churn_risk" | "at_risk_loyal";

interface GetCustomersParams {
  segment: CustomerType;
  size: number;
  page: number;
}

interface GetCustomersResponse {
  customers: Customer[];
  size: number;
  page: number;
  has_next: boolean;
}

export const getCustomers = async ({
  segment,
  size,
  page,
}: GetCustomersParams): Promise<GetCustomersResponse> => {
  try {
    const token = localStorage.getItem("authToken");

    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await api.get("/stores/customers", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      params: {
        segment: segment,
        size: size,
        page: page,
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const serverMessage =
        error.response.data.message || "CRM 고객 정보 서버 오류";
      throw new Error(serverMessage);
    }
    throw new Error("네트워크 연결 또는 서버 응답에 문제가 발생했습니다.");
  }
};

export const getCustomerDetail = async (
  customerId: number
): Promise<CustomerDetail> => {
  try {
    const token = localStorage.getItem("authToken");

    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await api.get(`/stores/customers/${customerId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const serverMessage =
        error.response.data.message || "고객 상세 정보 서버 오류";
      throw new Error(serverMessage);
    }
    throw new Error("네트워크 연결 또는 서버 응답에 문제가 발생했습니다.");
  }
};
