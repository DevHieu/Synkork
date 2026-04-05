import { Client, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/utils/auth";
import VueCookies from "vue-cookies";

const cookies = VueCookies as any;
let stompClient: Client | null = null;
const subscriptions = new Map<string, StompSubscription>();

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
          window.location.href = "/auth";
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
    if (stompClient?.connected) {
      onConnected?.();
      return;
    }

    let token = cookies.get("accessToken");
    if (!token) {
      try {
        token = await getFreshToken();
      } catch {
        window.location.href = "/auth";
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

    // Unsubscribe cái cũ nếu đã subscribe destination này rồi
    if (subscriptions.has(destination)) {
      subscriptions.get(destination)!.unsubscribe();
      subscriptions.delete(destination);
    }

    const sub = stompClient!.subscribe(destination, (msg) => {
      try {
        callback(JSON.parse(msg.body));
      } catch {
        callback(msg.body); // plain string
      }
    });

    subscriptions.set(destination, sub);
    return sub;
  },

  unsubscribeAll() {
    subscriptions.forEach((sub) => sub.unsubscribe());
    subscriptions.clear();
  },

  unsubscribeByDestination(destination: string) {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination)!.unsubscribe();
      subscriptions.delete(destination);
    }
  },

  publish(destination: string, body: any) {
    if (!this.isConnected()) return;
    stompClient!.publish({
      destination,
      body: JSON.stringify(body),
    });
  },
};
