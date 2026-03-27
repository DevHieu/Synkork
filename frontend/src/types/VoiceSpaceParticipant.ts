export interface Participant {
  userID: string;
  userName: string;
  videoStreamID?: string;
  videoOn: boolean;
  isLocal: boolean;
}
