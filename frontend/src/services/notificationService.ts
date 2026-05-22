// services/notificationService.ts
import axiosClient from "@/lib/axiosClient"

export const getNotifications = async () => {
    const res = await axiosClient.get('/api/notifications')
    return res.data
}

export const markNotificationAsRead = async (id: string) => {
    await axiosClient.patch(`/api/notifications/${id}/read`)
}

export const deleteNotification = async (id: string) => {
    const res = await axiosClient.delete(`/api/notifications/${id}`)
    return res.data
}