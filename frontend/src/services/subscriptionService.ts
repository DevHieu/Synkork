import axiosClient from "@/lib/axiosClient";

export const createPaymentLink = async (data: {
  plan: string;
  billingCycle: string;
}) => {
  const res = await axiosClient.post("/api/payment/vnpay", {
    plan: data.plan.toUpperCase(),
    billingCycle: data.billingCycle,
  });
  return res.data;
};

export interface PlanPricingItem {
  id: string;
  plan: "FREE" | "TEAM" | "BUSINESS";
  billingCycle: "MONTHLY" | "YEARLY";
  amount: number;
  discountType?: "PERCENTAGE" | "FIXED" | null;
  discountValue?: number | null;
  discountAmount?: number | null;
  finalAmount?: number | null;
  active: boolean;
  createdAt: string;
}

/**
 * Lấy bảng giá hiện tại (public API, không cần token).
 * GET /api/payment/plan-pricing
 */
export const getPlanPricing = async (): Promise<PlanPricingItem[]> => {
  const res = await axiosClient.get("/api/payment/plan-pricing");
  return res.data;
};
