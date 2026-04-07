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
