export type SpaceType = "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK";

export interface Space {
  id: string;
  name: string;
  type: SpaceType;
  roomType: "GROUP" | "DM" | "PERSONAL";
  restricted: boolean;
}
