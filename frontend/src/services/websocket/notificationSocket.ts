// notificationSocket.ts
import { socketService } from "./socketService"

export const notificationSocket = {

  async subscribeNotifications(userId: string, callback: (notification: any) => void) {
  console.log('>>> subscribeNotifications called, userId:', userId)  // ← thêm
  await socketService.connect()
  console.log('>>> socket connected:', socketService.isConnected())  // ← thêm

  return socketService.subscribe(
    `/user/queue/notifications`,
    (notification) => {
      console.log('>>> notification received!', notification)  // ← thêm
      callback(notification)
    }, 
    { persistent: true }
  )
},

  unsubscribeNotifications(userId: string) {
    socketService.unsubscribeByDestination(`/user/${userId}/queue/notifications`)
  }

}