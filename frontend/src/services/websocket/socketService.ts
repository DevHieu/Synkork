import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/utils/auth";
import VueCookies from "vue-cookies";

const cookies = VueCookies as any;
let stompClient: Client | null = null;

const createStompClient = (token: string, onConnected?: () => void): Client => {
  const client = new Client({
    webSocketFactory: () =>
      new SockJS(`${import.meta.env.VITE_BACKEND_URL}/api/ws`),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 0,
    onConnect: () => {
      onConnected?.();
    },
    onWebSocketClose: async (event) => {
      //bắt 401 và refresh token
      console.warn(`[Socket] Closed — code: ${event.code}`);

      const isUnauthorized =
        event.code === 4001 ||
        (event.reason ?? "").toLowerCase().includes("unauthorized");

      if (isUnauthorized) {
        cookies.remove("accessToken");
        try {
          const freshToken = await getFreshToken();

          stompClient = createStompClient(freshToken, onConnected);
          stompClient.activate();
        } catch {
          window.location.href = "/auth/login";
        }
      }
    },
    onStompError: (frame) => {
      console.error("[STOMP Error]", frame.headers["message"]);
    },
  });

  return client;
};

export const socketService = {
  async connect(onConnected?: () => void) {
    // Check xem có token chưa
    if (stompClient?.connected) return;

    let token = cookies.get("accessToken");
    if (!token) {
      try {
        token = await getFreshToken();
      } catch {
        window.location.href = "/auth/login";
        return;
      }
    }

    stompClient = createStompClient(token, onConnected);
    stompClient.activate();
  },

  getClient() {
    return stompClient;
  },

  isConnected() {
    return stompClient?.connected ?? false;
  },

  subscribe(destination: string, callback: (payload: any) => void) {
    if (!this.isConnected()) {
      console.error(
        `[Socket] Cannot subscribe to ${destination}. Not connected.`,
      );
      return null;
    }
    return stompClient!.subscribe(destination, (msg) => {
      callback(JSON.parse(msg.body));
    });
  },

  publish(destination: string, body: any) {
    if (!this.isConnected()) return;
    stompClient!.publish({
      destination,
      body: JSON.stringify(body),
    });
  },
};
