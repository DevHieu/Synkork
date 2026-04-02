export interface Participant {
  userID: string;
  userName: string;
  videoStreamID?: string;
  videoOn: boolean;
  micOn: boolean;
  audioOn: boolean;
  isLocal: boolean;
}
