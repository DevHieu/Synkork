import type { PlanType } from "@/utils/PlanLimitUtils";

export interface Room {
  id: string;
  name: string;
  description: string;
  roomAvatar: string;
  currentPlan: PlanType;
}
