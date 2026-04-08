import { socketService } from "./socketService"

export const noteSocket = {
    subscribeCreateNote(spaceId: string, callback: (payload: any) => void) {
        return socketService.subscribe(`/topic/space/${spaceId}/notes/create`, callback)
    }
}