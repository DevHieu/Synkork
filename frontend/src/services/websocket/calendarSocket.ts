import { socketService } from "./socketService";

export const subscribeCalendarSpace = (spaceId: string, onEvent: (payload: any) => void) => {
  return socketService.subscribe(`/topic/space/${spaceId}/calendar`, onEvent);
};