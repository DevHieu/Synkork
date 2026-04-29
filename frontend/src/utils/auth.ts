import VueCookies from "vue-cookies";
import axiosClient from "@/lib/axiosClient";

const cookies = VueCookies as any;

export async function getFreshToken(): Promise<string> {
  const response = await axiosClient.post(
    "/api/auth/refresh",
    {},
    { withCredentials: true }
  );
  const accessToken = response.data;
  cookies.set("accessToken", accessToken, "15m");
  return accessToken;
}

export const getUserIdFromToken = (): string | null => {
  const token = cookies.get("accessToken") // ✅ đúng chỗ

  if (!token) return null

  try {
    const payload = JSON.parse(atob(token.split(".")[1]))
    return payload.userId
  } catch (e) {
    console.error("Decode token failed", e)
    return null
  }
}
