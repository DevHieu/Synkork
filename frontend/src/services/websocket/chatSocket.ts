import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient: Client;
let spaceSubscription: any = null;

export function connectWebSocket(onConnected?: () => void) {
  stompClient = new Client({
    webSocketFactory: () =>
      new SockJS(`${import.meta.env.VITE_BACKEND_URL}/api/ws`),

    reconnectDelay: 5000,
    debug: (str) => console.log(str),

    onConnect: () => {
      console.log("WebSocket connected");
      onConnected?.();
    },

    onStompError: (frame) => {
      console.error("Broker error:", frame.headers["message"]);
    },
  });

  stompClient.activate();
}

export function subscribeSpace(spaceId: string, onMessage: (msg: any) => void) {
  console.log(spaceId);

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

export function addUserToSocketRoom(userId: string) {
  if (!stompClient?.connected) return;

  stompClient.publish({
    destination: "/app/chat.addUser",
    body: userId,
  });
}
