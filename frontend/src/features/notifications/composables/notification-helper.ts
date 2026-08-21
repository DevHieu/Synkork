import type { NotificationDTO } from "@/features/notifications/types/Notification";

type NotificationContext = "dropdown" | "toast";

const MESSAGES: Record<string, Record<NotificationContext, string>> = {
  CARD_ASSIGNED: {
    dropdown: "Vừa assign bạn vào một task",
    toast: "Bạn vừa được assign vào một task",
  },
  CARD_DUE_SOON: {
    dropdown: "Nhắc bạn: task sắp đến hạn",
    toast: "Nhắc nhở: Task của bạn sắp đến hạn",
  },
  CARD_OVER_DUE: {
    dropdown: "Nhắc bạn: task đã quá hạn",
    toast: "Nhắc nhở: Task của bạn đã quá hạn",
  },
  FRIEND_REQUEST: {
    dropdown: "Đã gửi cho bạn lời mời kết bạn",
    toast: "Bạn có lời mời kết bạn mới",
  },
  FRIEND_REJECT: {
    dropdown: "Đã từ chối lời mời kết bạn của bạn",
    toast: "Lời mời kết bạn của bạn đã bị từ chối",
  },
  FRIEND_ACCEPT: {
    dropdown: "Đã chấp nhận lời mời kết bạn của bạn",
    toast: "Lời mời kết bạn của bạn đã được chấp nhận",
  },
  NOTE_REMINDER: {
    dropdown: "Nhắc bạn: ghi chú sắp đến hạn",
    toast: "Nhắc nhở: Ghi chú sắp đến hạn",
  },
  EVENT_REMINDER: {
    dropdown: "Nhắc bạn: sự kiện sắp diễn ra",
    toast: "Nhắc nhở: Sự kiện sắp diễn ra",
  },
  EVENT_ASSIGNED: {
    dropdown: "Vừa assign bạn vào một sự kiện",
    toast: "Bạn vừa được assign vào một sự kiện",
  },
};

const DEFAULT_MESSAGE: Record<NotificationContext, string> = {
  dropdown: "Có thông báo mới",
  toast: "Bạn có thông báo mới",
};

export function notificationMessage(
  n: NotificationDTO,
  context: NotificationContext = "dropdown",
) {
  return MESSAGES[n.refType]?.[context] ?? DEFAULT_MESSAGE[context];
}

export function getNotificationPath(n: NotificationDTO) {
  if (n.type === "FRIEND") return "/me/friends";

  // if (!n.roomId || !n.spaceId) return null

  switch (n.type) {
    case "TASK":
      return `/rooms/task/${n.roomId}/${n.spaceId}`;

    case "NOTE":
      return `/rooms/note/${n.roomId}/${n.spaceId}`;

    case "CALENDAR":
      return `/rooms/calendar/${n.roomId}/${n.spaceId}`;

    case "CHAT":
      return `/rooms/chat/${n.roomId}/${n.spaceId}`;

    default:
      return null;
  }
}

export function timeAgo(dateStr: string | number): string {
  const date =
    typeof dateStr === "number" ? new Date(dateStr) : new Date(dateStr);
  const diff = Date.now() - date.getTime();
  const minutes = Math.floor(diff / 60000);

  if (minutes < 1) return "Vừa xong";
  if (minutes < 60) return `${minutes} phút trước`;

  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} giờ trước`;

  return `${Math.floor(hours / 24)} ngày trước`;
}
