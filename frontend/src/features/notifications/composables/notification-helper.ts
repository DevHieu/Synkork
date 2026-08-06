import type { NotificationDTO } from "@/types/Notification";

export function notificationMessage(n: NotificationDTO) {
  switch (n.refType) {
    case "CARD_ASSIGNED":
      return "Bạn vừa được assign vào một task";
    case "CARD_DUE_SOON":
      return "Nhắc nhở: Task của bạn sắp đến hạn";
    case "CARD_OVER_DUE":
      return "Nhắc nhở: Task của bạn đã quá hạn";

    case "FRIEND_REQUEST":
      return "Bạn có lời mời kết bạn mới";
    case "FRIEND_REJECT":
      return "Lời mời kết bạn của bạn đã bị từ chối";
    case "FRIEND_ACCEPT":
      return "Lời mời kết bạn của bạn đã được chấp nhận";

    case "NOTE_REMINDER":
      return "Nhắc nhở: Ghi chú sắp đến hạn";

    case "EVENT_REMINDER":
      return "Nhắc nhở: Sự kiện sắp diễn ra";

    default:
      return "Bạn có thông báo mới";
  }
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
  const date = typeof dateStr === 'number' ? new Date(dateStr) : new Date(dateStr)
  const diff = Date.now() - date.getTime()
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1)  return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`

  const hours = Math.floor(minutes / 60)
  if (hours < 24)   return `${hours} giờ trước`

  return `${Math.floor(hours / 24)} ngày trước`
}