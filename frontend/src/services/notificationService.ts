// services/notificationService.ts
import axiosClient from "@/lib/axiosClient"
import type { NotificationDTO } from "@/types/Notification"

export const getNotifications = async (): Promise<NotificationDTO[]> => {
    const res = await axiosClient.get('/api/notifications')
    return res.data
}

export const markNotificationAsRead = async (id: string): Promise<void> => {
    await axiosClient.patch(`/api/notifications/${id}/read`)
}