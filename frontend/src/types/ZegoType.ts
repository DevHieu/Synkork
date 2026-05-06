import type { Ref } from "vue";
import type { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "./VoiceSpaceParticipant";

export interface ZegoState {
  zg: ZegoExpressEngine | null;
  localAudioStream: any;
  localAudioStreamID: string;
  localVideoStream: any;
  localVideoStreamID: string;
  localScreenStream: any;
  localScreenStreamID: string;
}

export interface ZegoServiceOptions {
  state: ZegoState;
  appID: number;
  server: string;
  participants: Ref<Map<string, Participant>>;
  remoteStreams: Map<string, any>;
  videoOn: Ref<boolean>;
  micOn: Ref<boolean>;
  audioOn: Ref<boolean>;
  screenOn: Ref<boolean>;
  isMuted: Ref<boolean>;
  isDeafen: Ref<boolean>;
}
