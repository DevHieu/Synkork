import axiosClient from "@/lib/axiosClient";
import { setCookie } from "@/lib/cookies";

let refreshPromise: Promise<string> | null = null;

export async function getFreshToken(): Promise<string> {
  // Nếu đang có 1 lệnh refresh chạy rồi thì dùng chung kết quả đó
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    try {
      const res = await axiosClient.post(
        "/api/auth/refresh",
        {},
        { withCredentials: true },
      );
      const newToken = res.data.accessToken;
      setCookie("accessToken", newToken);
      return newToken;
    } finally {
      // Xong (thành công hay fail) đều phải xoá promise
      // để lần refresh tiếp theo tạo request mới
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}
