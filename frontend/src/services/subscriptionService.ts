import axiosClient from "@/lib/axiosClient";

export const createPaymentLink = async (data: { plan: string; billingCycle: string }) => {
  const res = await axiosClient.post("/api/payment/momo", {
    plan: data.plan.toUpperCase(),
    billingCycle: data.billingCycle,
  });
  return res.data;
}