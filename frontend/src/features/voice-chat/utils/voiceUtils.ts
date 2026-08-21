import type { Participant, VoiceItemType } from "../types/VoiceTypes";

export const getVideoContainerId = (item: VoiceItemType) => {
  if (item.type === "screen")
    return item.isLocal
      ? "screen-sharing-container"
      : `remote-screen-${item.userID}`;
  return item.isLocal ? "local-video-container" : `remote-video-${item.userID}`;
};

export const generateScreenItem = (p: Participant) => {
  return {
    id: `screen-${p.userID}`,
    audioId: p.audioStreamID,
    type: "screen" as const,
    userID: p.userID,
    isLocal: p.isLocal,
    userName: p.userName,
    videoOn: false,
    micOn: true,
    audioOn: true,
    muted: p.muted,
    deafen: p.deafen,
  };
};

export const generateParticipantItem = (p: Participant) => {
  return {
    id: `participant-${p.userID}`,
    audioId: p.audioStreamID,
    type: "participant" as const,
    userID: p.userID,
    isLocal: p.isLocal,
    userName: p.userName,
    videoOn: p.videoOn,
    micOn: p.micOn,
    audioOn: p.audioOn,
    muted: p.muted,
    deafen: p.deafen,
  };
};
