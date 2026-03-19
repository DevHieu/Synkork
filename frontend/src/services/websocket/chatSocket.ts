import { getFreshToken } from "@/utils/auth";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient: Client;
let spaceSubscription: any = null;

import VueCookies from "vue-cookies";

const cookies = VueCookies as any;

export function connectWebSocket(onConnected?: () => void) {
  stompClient = new Client({
    webSocketFactory: () =>
      new SockJS(`${import.meta.env.VITE_BACKEND_URL}/api/ws`),

    connectHeaders: {
      Authorization: `Bearer ${cookies.get("accessToken")}`,
    },

    reconnectDelay: 5000,
    debug: (str) => console.log(str),

    onConnect: () => {
      console.log("WebSocket connected");
      onConnected?.();
    },

    onStompError: async (frame) => {
      const message: string = frame.headers["message"] ?? "";
      console.error("Broker error:", message);

      if (message.toLowerCase().includes("jwt expired")) {
        console.log("Access token expired — refreshing and reconnecting...");

        // Force-clear the stale token so getFreshToken hits the refresh endpoint
        cookies.remove("accessToken");

        try {
          const freshToken = await getFreshToken();

          if (stompClient) {
            stompClient.connectHeaders = {
              Authorization: `Bearer ${freshToken}`,
            };
            stompClient.deactivate().then(() => stompClient?.activate());
          }
        } catch {
          cookies.remove("accessToken");
          window.location.href = "/auth/login";
        }
      }
    },
  });

  stompClient.activate();
}

export function subscribeSpace(spaceId: string, onMessage: (msg: any) => void) {
  if (!stompClient?.connected) return;

  if (spaceSubscription) {
    spaceSubscription.unsubscribe();
    spaceSubscription = null;
  }

  spaceSubscription = stompClient.subscribe(
    `/topic/space/${spaceId}/messages`,
    (message) => {
      console.log("subscribeComplete");

      onMessage(JSON.parse(message.body));
    }
  );
}

export function sendMessage(message: { content: string; spaceId: string }) {
  if (!stompClient?.connected) return;

  stompClient.publish({
    destination: "/app/chat.sendMessage",
    body: JSON.stringify(message),
  });
}
