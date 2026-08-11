import type { Space, SpaceType } from "../types/Space";

export const groupSpacesByType = async (spaces: Space[]) => {
  const result = {
    CHAT: [],
    VOICE: [],
    NOTE: [],
    CALENDAR: [],
    TASK: [],
  } as Record<string, any[]>;

  spaces.forEach((space) => {
    result[space.type as SpaceType]?.push(space);
  });

  return result;
};
