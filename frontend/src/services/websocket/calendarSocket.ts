import { getStompClient, isWebSocketConnected } from "./chatSocket";

let calendarSubscription: any = null;

export function subscribeCalendarSpace(
    spaceId: string,
    onEvent: (payload: { action: string; event: any }) => void
) {
    const client = getStompClient();
    if (!client?.connected) {
        console.warn("WebSocket not connected, cannot subscribe to calendar");
        return;
    }

    // Hủy subscription cũ nếu có
    unsubscribeCalendarSpace();

    calendarSubscription = client.subscribe(
        `/topic/space/${spaceId}/calendar`,
        (message) => {
            const payload = JSON.parse(message.body);
            console.log("📅 Calendar update:", payload.action);
            onEvent(payload);
        }
    );
}

export function unsubscribeCalendarSpace() {
    if (calendarSubscription) {
        calendarSubscription.unsubscribe();
        calendarSubscription = null;
    }
}

export { isWebSocketConnected };
