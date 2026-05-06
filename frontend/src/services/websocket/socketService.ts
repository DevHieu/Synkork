import { Client, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getFreshToken } from "@/utils/auth";
import VueCookies from "vue-cookies";

const cookies = VueCookies as any;
let stompClient: Client | null = null;
const subscriptions = new Map<string, StompSubscription>();
let connectingPromise: Promise<void> | null = null;
const persistentDestinations = new Set<string>();

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
        event.code === 1002 ||
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
      return;
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
      let token = cookies.get("accessToken");
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
    if (!this.isConnected()) {
      console.error(
        `[Socket] Cannot subscribe to ${destination}. Not connected.`,
      );
      return null;
    }

    if (subscriptions.has(destination)) {
      subscriptions.get(destination)!.unsubscribe();
      subscriptions.delete(destination);
    }

    const sub = stompClient!.subscribe(destination, (msg) => {
      try {
        callback(JSON.parse(msg.body));
      } catch {
        callback(msg.body);
      }
    });

    subscriptions.set(destination, sub);

    // Đánh dấu persistent nếu có
    if (options?.persistent) {
      persistentDestinations.add(destination);
    }

    return sub;
  },

  // unsubscribeAll bỏ qua persistent
  unsubscribeAll() {
    console.log("changing");

    subscriptions.forEach((sub, destination) => {
      if (!persistentDestinations.has(destination)) {
        console.log("subscribe: ", sub);

        sub.unsubscribe();
        subscriptions.delete(destination);
      }
    });
  },

  unsubscribeAllForce() {
    subscriptions.forEach((sub) => sub.unsubscribe());
    subscriptions.clear();
    persistentDestinations.clear();
  },

  unsubscribeByDestination(destination: string) {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination)!.unsubscribe();
      subscriptions.delete(destination);
      persistentDestinations.delete(destination);
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
