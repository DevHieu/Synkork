import type { Message } from "@/types/Message";
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

// Ở đây sẽ có 2 phần chính cho mỗi chức năng

// 1. publish: thực hiện chức năng. (VD: đây là thực hienej gửi message đi)
export function sendMessage(message: { content: string; spaceId: string }) {
  if (!stompClient?.connected) return;

  stompClient.publish({
    destination: "/app/chat.sendMessage",
    body: JSON.stringify(message),
  });
}

// 2. subscribe: Lắng nghe. Khi có thay đổi thì hàm này thực hiện (VD: message có thì hàm này đc gọi để nhận message mới)
export function subscribeGetMessage(
  spaceId: string,
  onMessage: (msg: any) => void,
) {
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
    },
  );
}

export const deleteMessage = (message: Message) => {
  if (!stompClient?.connected) return;

  stompClient.publish({
    destination: "/app/chat.deleteMessage",
    body: JSON.stringify(message),
  });
};

export const subscribeDelete = (
  spaceId: string,
  callback: (messageId: string) => void, // callback hàm mình sẽ truyền ở chỗ mình gọi cái subcribe này
) => {
  stompClient.subscribe(
    `/topic/space/${spaceId}/messages/delete`,
    (message) => {
      const messageId = JSON.parse(message.body); // server trả về json nên phải parse về string
      callback(messageId);
    },
  );
};

export const updateMessage = (message: Message) => {
  if (!stompClient?.connected) return;

  stompClient.publish({
    destination: "/app/chat.updateMessage",
    body: JSON.stringify(message),
  });
};

export const subscribeUpdate = (
  spaceId: string,
  callback: (updatedMessage: Message) => void,
) => {
  stompClient.subscribe(
    `/topic/space/${spaceId}/messages/update`,
    (message) => {
      const updatedMessage = JSON.parse(message.body) as Message;
      callback(updatedMessage);
    },
  );
};
