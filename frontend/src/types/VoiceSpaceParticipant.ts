export interface Participant {
  userID: string;
  userName: string;
  videoStreamID?: string;
  screenStreamID?: string;
  videoOn: boolean;
  micOn: boolean;
  audioOn: boolean;
  screenOn: boolean;
  isLocal: boolean;
  avatarUrl?: string;
}

export interface VoiceItemType {
  id: string;
  type: "screen" | "participant";
  userID: string;
  isLocal: boolean;
  userName: string;
  videoOn: boolean;
  micOn: boolean;
  audioOn: boolean;
}
