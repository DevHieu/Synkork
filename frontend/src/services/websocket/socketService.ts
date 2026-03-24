import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/utils/auth";
import VueCookies from "vue-cookies";

const cookies = VueCookies as any;
let stompClient: Client | null = null;

export const socketService = {
  // Khởi tạo kết nối
  connect(onConnected?: () => void) {
    if (stompClient?.connected) return;

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_BACKEND_URL}/api/ws`),
      connectHeaders: {
        Authorization: `Bearer ${cookies.get("accessToken")}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("🚀 [Socket] Connected");
        onConnected?.();
      },
      onStompError: async (frame) => {
        const message = frame.headers["message"] ?? "";
        if (message.toLowerCase().includes("jwt expired")) {
          console.warn(" [Socket] Token expired, refreshing...");
          cookies.remove("accessToken");
          try {
            const freshToken = await getFreshToken();
            if (stompClient) {
              stompClient.connectHeaders = { Authorization: `Bearer ${freshToken}` };
              stompClient.deactivate().then(() => stompClient?.activate());
            }
          } catch {
            window.location.href = "/auth/login";
          }
        }
      },
    });

    stompClient.activate();
  },

  // Getter để các file khác lấy client
  getClient() {
    return stompClient;
  },

  // Kiểm tra trạng thái
  isConnected() {
    return stompClient?.connected ?? false;
  },

  // Hàm hỗ trợ Subscribe an toàn (Quan trọng)
  subscribe(destination: string, callback: (payload: any) => void) {
    if (!this.isConnected()) {
      console.error(` [Socket] Cannot subscribe to ${destination}. Not connected.`);
      return null;
    }
    return stompClient!.subscribe(destination, (msg) => {
      callback(JSON.parse(msg.body));
    });
  },

  // Hàm hỗ trợ Publish (Gửi dữ liệu)
  publish(destination: string, body: any) {
    if (!this.isConnected()) return;
    stompClient!.publish({
      destination,
      body: JSON.stringify(body),
    });
  }
};