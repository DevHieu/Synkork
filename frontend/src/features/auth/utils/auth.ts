import axiosClient from "@/lib/axiosClient";
import { setCookie } from "@/lib/cookies";

export async function getFreshToken(): Promise<string> {
  const response = await axiosClient.post(
    "/api/auth/refresh",
    {},
    { withCredentials: true },
  );
  const accessToken = response.data.accessToken;
  setCookie("accessToken", accessToken);
  return accessToken;
}
