import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import VueCookies from "vue-cookies";
import { getFreshToken } from "@/utils/auth";

const cookies = VueCookies as any;
let stompClient: Client;
let boardSubscription: any = null;

export function connectTaskWebSocket(onConnected?: () => void) {
  stompClient = new Client({
    // Sử dụng endpoint chuẩn của bạn: /ws
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

    connectHeaders: {
      Authorization: `Bearer ${cookies.get("accessToken")}`,
    },

    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: () => {
      console.log("✅ Task WebSocket connected");
      onConnected?.();
    },

    onStompError: async (frame) => {
      const message = frame.headers["message"] ?? "";
      if (message.toLowerCase().includes("jwt expired")) {
        // Logic refresh token giống bên Chat của bạn
        cookies.remove("accessToken");
        try {
          const freshToken = await getFreshToken();
          stompClient.connectHeaders = { Authorization: `Bearer ${freshToken}` };
          stompClient.deactivate().then(() => stompClient?.activate());
        } catch {
          window.location.href = "/auth/login";
        }
      }
    },
  });

  stompClient.activate();
}

// Hàm Subscribe vào Board/Space để nhận các sự kiện: TASK_UPDATED, COLUMN_CREATED...
export function subscribeTaskBoard(spaceId: string, onEventReceived: (event: any) => void) {
  if (!stompClient?.connected) {
    // Nếu chưa connect thì đợi 1 chút rồi subscribe lại hoặc báo lỗi
    console.warn("STOMP chưa kết nối, đang đợi...");
    return;
  }

  if (boardSubscription) {
    boardSubscription.unsubscribe();
  }

  // Theo code Java của bạn: messagingTemplate.convertAndSend("/topic/space/" + spaceId, ...)
  boardSubscription = stompClient.subscribe(
    `/topic/space/${spaceId}`, 
    (message) => {
      const event = JSON.parse(message.body);
      console.log("📡 Task Event Received:", event.type);
      onEventReceived(event);
    }
  );
}

export function disconnectTaskWebSocket() {
  if (boardSubscription) boardSubscription.unsubscribe();
  if (stompClient) stompClient.deactivate();
}