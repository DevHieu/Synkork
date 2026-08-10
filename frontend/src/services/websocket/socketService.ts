import { Client, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/utils/auth";
import { getCookie, removeCookie } from "@/lib/cookies";

let stompClient: Client | null = null;
const subscriptions = new Map<string, StompSubscription>();
const activeSubscriptions = new Map<string, { callback: (payload: any) => void; options?: { persistent?: boolean } }>();
let connectingPromise: Promise<void> | null = null;
// Giữ lại các kênh cần sống lâu hơn vòng đời của từng space.
const persistentDestinations = new Set<string>();

const doSubscribe = (destination: string, callback: (payload: any) => void) => {
  if (!stompClient?.connected) return;
  if (subscriptions.has(destination)) {
    subscriptions.get(destination)!.unsubscribe();
  }
  const sub = stompClient.subscribe(destination, (msg) => {
    try {
      callback(JSON.parse(msg.body));
    } catch {
      callback(msg.body);
    }
  });
  subscriptions.set(destination, sub);
};

const createStompClient = (token: string, onConnected?: () => void): Client => {
  const client = new Client({
    webSocketFactory: () =>
      new SockJS(`${import.meta.env.VITE_BACKEND_URL}/api/ws`),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    heartbeatIncoming: 10000, // mong nhận heartbeat từ server mỗi 10s
    heartbeatOutgoing: 10000, // gửi heartbeat cho server mỗi 10s
    reconnectDelay: 5000, // tự động reconnect sau 5s nếu mất kết nối

    onConnect: () => {
      onConnected?.();
      // Khôi phục tất cả đăng ký sau khi kết nối lại
      activeSubscriptions.forEach(({ callback }, destination) => {
        doSubscribe(destination, callback);
      });
    },
    onWebSocketClose: async (event) => {
      //bắt 401 và refresh token
      console.warn(`[Socket] Closed — code: ${event.code}`);

      const isUnauthorized =
        event.code === 4001 ||
        event.code === 1002 ||
        (event.reason ?? "").toLowerCase().includes("unauthorized");

      if (isUnauthorized) {
        removeCookie("accessToken");
        try {
          const freshToken = await getFreshToken();

          stompClient = createStompClient(freshToken, onConnected);
          stompClient.activate();
        } catch {
          window.location.href = "/auth";
        }
      }
    },
    onStompError: async (frame) => {
      const message = frame.headers["message"] ?? "";

      console.error("[STOMP Error]", message);

      const isAuthError =
        message.includes("JWT validation failed") ||
        message.toLowerCase().includes("unauthorized");

      if (isAuthError) {
        removeCookie("accessToken");

        try {
          const freshToken = await getFreshToken();

          stompClient = createStompClient(freshToken);
          stompClient.activate();
        } catch {
          window.location.href = "/auth";
        }
      }
    },
  });

  return client;
};

export const socketService = {
  async connect(): Promise<void> {
    if (stompClient?.connected) return;

    // Nếu đang connecting rồi thì chờ cái đó, không tạo mới
    if (connectingPromise) return connectingPromise;

    connectingPromise = new Promise<void>(async (resolve, reject) => {
      let token = getCookie("accessToken");
      if (!token) {
        try {
          token = await getFreshToken();
        } catch {
          window.location.href = "/auth";
          reject();
          return;
        }
      }

      stompClient = createStompClient(token, () => {
        connectingPromise = null;
        resolve();
      });
      stompClient.activate();
    });

    return connectingPromise;
  },

  getClient() {
    return stompClient;
  },

  isConnected() {
    return stompClient?.connected ?? false;
  },

  subscribe(
    destination: string,
    callback: (payload: any) => void,
    options?: { persistent?: boolean },
  ) {
    activeSubscriptions.set(destination, { callback, options });

    if (options?.persistent) {
      persistentDestinations.add(destination);
    }

    if (this.isConnected()) {
      doSubscribe(destination, callback);
    }

    return {
      unsubscribe: () => {
        this.unsubscribeByDestination(destination);
      }
    };
  },

  // unsubscribeAll bỏ qua persistent
  unsubscribeAll() {
    activeSubscriptions.forEach((_, destination) => {
      if (!persistentDestinations.has(destination)) {
        if (subscriptions.has(destination)) {
          subscriptions.get(destination)!.unsubscribe();
          subscriptions.delete(destination);
        }
        activeSubscriptions.delete(destination);
      }
    });
  },

  unsubscribeAllForce() {
    subscriptions.forEach((sub) => sub.unsubscribe());
    subscriptions.clear();
    activeSubscriptions.clear();
    persistentDestinations.clear();
  },

  unsubscribeByDestination(destination: string) {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination)!.unsubscribe();
      subscriptions.delete(destination);
    }
    activeSubscriptions.delete(destination);
    persistentDestinations.delete(destination);
  },

  publish(destination: string, body: any) {
    if (!this.isConnected()) return;
    stompClient!.publish({
      destination,
      body: JSON.stringify(body),
    });
  },
};
