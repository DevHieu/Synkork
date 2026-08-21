import type { CardEvent, ColumnEvent } from "@/features/tasks/types/Task";
import { socketService } from "../../../services/socketService";

const subscribedColumns = new Set<string>();
export const taskSocket = {
  subscribeCardUpdate(columnId: string, callback: (card: any) => void) {
    subscribedColumns.add(columnId);
    return socketService.subscribe(
      `/topic/space/${columnId}/card/update`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeCardCreate(columnId: string, callback: (card: any) => void) {
    subscribedColumns.add(columnId);
    return socketService.subscribe(
      `/topic/space/${columnId}/card/create`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeCardDelete(columnId: string, callback: (cardId: string) => void) {
    subscribedColumns.add(columnId);
    return socketService.subscribe(
      `/topic/space/${columnId}/card/delete`,
      (cardId: string) => {
        callback(cardId);
      },
    );
  },

  subscribeCardMove(columnId: string, callback: (card: any) => void) {
    subscribedColumns.add(columnId);
    return socketService.subscribe(
      `/topic/space/${columnId}/card/move`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeCardDeleteArchived(spaceId: string, callback: (card: any) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/card/deleteAllArchived`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeColumnUpdate(spaceId: string, callback: (column: any) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/update`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeColumnCreate(spaceId: string, callback: (column: any) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/create`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeColumnDelete(spaceId: string, callback: (columnId: string) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/delete`,
      (columnId: string) => {
        callback(columnId);
      },
    );
  },

  subscribeColumnMove(spaceId: string, callback: (column: any) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/move`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeColumnDeleteArchived(
    spaceId: string,
    callback: (column: any) => void,
  ) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/deleteAllArchived`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeCardArchive(spaceId: string, callback: (card: CardEvent) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/card/archive`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeCardUnarchive(spaceId: string, callback: (card: CardEvent) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/card/unarchive`,
      (card) => {
        callback(card);
      },
    );
  },

  subscribeColumnArchive(
    spaceId: string,
    callback: (column: ColumnEvent) => void,
  ) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/archive`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeColumnUnarchive(
    spaceId: string,
    callback: (column: ColumnEvent) => void,
  ) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/column/unarchive`,
      (column) => {
        callback(column);
      },
    );
  },

  subscribeCardComplete(spaceId: string, callback: (card: CardEvent) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/card/complete`,
      callback,
    );
  },

  leaveSpace(spaceId: string) {
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/update`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/create`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/delete`,
    );
    socketService.unsubscribeByDestination(`/topic/space/${spaceId}/card/move`);
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/archive`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/unarchive`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/deleteAllArchived`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/card/complete`,
    );

    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/update`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/create`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/delete`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/move`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/archive`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/unarchive`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/column/deleteAllArchived`,
    );

    subscribedColumns.clear();
  },
};
