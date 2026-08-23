import { Client, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/features/auth/utils/auth";
import { getCookie, removeCookie } from "@/lib/cookies";

let stompClient: Client | null = null;
const subscriptions = new Map<string, StompSubscription>();
const activeSubscriptions = new Map<
  string,
  { callback: (payload: any) => void; options?: { persistent?: boolean } }
>();
let connectingPromise: Promise<void> | null = null;
let isReconnecting = false;

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
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    reconnectDelay: 5000,

    onConnect: () => {
      isReconnecting = false; // reconnect thành công, mở khoá lại
      onConnected?.();
      activeSubscriptions.forEach(({ callback }, destination) => {
        doSubscribe(destination, callback);
      });
    },
    onWebSocketClose: async (event) => {
      console.warn(`[Socket] Closed — code: ${event.code}`);

      const isUnauthorized =
        event.code === 4001 ||
        event.code === 1002 ||
        (event.reason ?? "").toLowerCase().includes("unauthorized");

      if (isUnauthorized) {
        await reconnectWithFreshToken(client, onConnected);
      }
    },
    onStompError: async (frame) => {
      const message = frame.headers["message"] ?? "";
      console.error("[STOMP Error]", message);

      const isAuthError =
        message.includes("JWT validation failed") ||
        message.toLowerCase().includes("unauthorized");

      if (isAuthError) {
        await reconnectWithFreshToken(client, onConnected);
      }
    },
  });

  return client;
};

const reconnectWithFreshToken = async (
  oldClient: Client,
  onConnected?: () => void,
) => {
  if (isReconnecting) return; // đang có 1 lần reconnect chạy rồi, bỏ qua
  isReconnecting = true;

  removeCookie("accessToken");

  // tắt hẳn client cũ trước, nếu ko nó vẫn tự reconnect theo reconnectDelay riêng
  try {
    await oldClient.deactivate();
  } catch (e) {
    console.warn("[Socket] Failed to deactivate old client", e);
  }

  try {
    const freshToken = await getFreshToken();
    stompClient = createStompClient(freshToken, onConnected);
    stompClient.activate();
  } catch {
    isReconnecting = false;
    window.location.href = "/auth";
  }
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
      },
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
