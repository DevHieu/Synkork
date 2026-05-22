import axiosClient from "@/lib/axiosClient";
import { setCookie } from "@/lib/cookies";

export async function getFreshToken(): Promise<string> {
  const response = await axiosClient.post(
    "/api/auth/refresh",
    {},
    { withCredentials: true },
  );
  const accessToken = response.data;
  setCookie("accessToken", accessToken, 60 * 60 * 15); // 15 minutes
  return accessToken;
}
