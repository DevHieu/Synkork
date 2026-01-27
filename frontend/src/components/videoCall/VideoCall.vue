<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";

const appID = 1910464485;
const server = "b370ef3adb4af7dd10a1a9c216f63e18";

const zg = new ZegoExpressEngine(appID, server);

const userID = sessionStorage.getItem("userId") || "user_" + Date.now();
const userName = sessionStorage.getItem("username") || "Test User";
const roomID = "0001";
const token =
  "04AAAAAGlro28ADPrmlUFKLdwZte4sGACsKr9GnvNrSVDBPivZ8l2Pdb9pbMtPNqQVck/wTOCM+NthRYPZl+pypRtFoV405MTnNtA7WuaMig+sHEEz+RA5VHBsCwmZZutRvq0Udc0krzrknNAEuLuerg0Oe4EgJus3We/683MbShIRu1n1h3MKcfQq9aLUYVTaEvymtvGbe4luRJH1WfbMLfjSSZXe2XqLNuge4FX6Oo05SWoMEqO2APj4J82VHaliginlmgE="; // token của mày

onMounted(async () => {
  console.log("mounted");

  // check môi trường
  const sys = await zg.checkSystemRequirements();
  console.log("system:", sys);

  // lắng nghe state
  zg.on("roomStateChanged", async (_, reason) => {
    console.log("room state:", reason);

    if (reason === "LOGINED") {
      console.log("login ok");

      const localStream = await zg.createStream({
        camera: { audio: true, video: true },
      });

      console.log("local stream:", localStream);

      document
        .getElementById("local-video")
        ?.replaceChildren(localStream.video);

      await zg.startPublishingStream("stream_" + userID, localStream);
      await zg.createLocalStreamView(localStream).play("local-video");

      // localStream.playVideo(document.querySelector("#local-video"));

      // // Start to publish an audio and video stream to the ZEGOCLOUD audio and video cloud.
      // let streamID = new Date().getTime().toString();
      // zg.startPublishingStream(streamID, localStream);
    } else {
      console.log("error in room:", reason);
      console.log("login success" + userID);
      console.log("username: " + userName);
      console.log("token: " + token);
    }
  });

  zg.loginRoom(
    roomID,
    token,
    { userID: userID, userName: userName },
    { userUpdate: true }
  ).then((result) => {
    if (result == true) {
      console.log("login success: " + result);
    }
  });

  zg.on(
    "roomStreamUpdate",
    async (roomID, updateType, streamList, extendedData) => {
      // When `updateType` is set to `ADD`, an audio and video stream is added, and you can call the `startPlayingStream` method to play the stream.
      if (updateType == "ADD") {
        // When streams are added, play them.
        // For the conciseness of the sample code, only the first stream in the list of newly added audio and video streams is played here. In a real service, it is recommended that you traverse the stream list to play each stream.
        const streamID = streamList[0].streamID;
        // The stream list specified by `streamList` contains the ID of the corresponding stream.
        const remoteStream = await zg.startPlayingStream(streamID);

        // Create a media stream player object to play remote media streams.
        const remoteView = zg.createRemoteStreamView(remoteStream);
        // Mount the player to a page. In the sample code, `remote-video` indicates the DOM element ID of the player.
        remoteView.play("remote-video");
      } else if (updateType == "DELETE") {
        // When streams are deleted, stop playing them.
      }
    }
  );
});
</script>

<template>
  <div class="w-full h-screen bg-zinc-900 flex flex-col">
    <!-- Video area -->
    <div class="flex-1 relative flex items-center justify-center">
      <!-- Remote video -->
      <div
        id="remote-video"
        class="w-[70%] h-[70%] bg-black rounded-xl border border-zinc-700 flex items-center justify-center text-zinc-500"
      >
        Remote Video
      </div>

      <!-- Local video (PiP) -->
      <div
        id="local-video"
        class="absolute bottom-6 right-6 w-56 h-40 bg-black rounded-lg border border-zinc-600 flex items-center justify-center text-zinc-400"
      >
        Local Video
      </div>
    </div>

    <!-- Control bar -->
    <div
      class="h-20 bg-zinc-800 border-t border-zinc-700 flex items-center justify-center gap-4"
    >
      <button class="btn">🎤</button>
      <button class="btn">🎥</button>
      <button class="btn btn-danger">⛔</button>
    </div>
  </div>
</template>

<style scoped>
h1,
h4 {
  text-align: center;
}

#local-video,
#remote-video {
  width: 400px;
  height: 300px;
  border: 1px solid #dfdfdf;
}

#local-video {
  position: relative;
  margin: 0 auto;
  display: block;
}

#remote-video {
  display: flex;
  margin: auto;
  position: relative !important;
}

.btn {
  width: 48px;
  height: 48px;
  border-radius: 9999px;
  background: #3f3f46;
  color: white;
  font-size: 20px;
  transition: background 0.2s;
}

.btn:hover {
  background: #52525b;
}

.btn-danger {
  background: #dc2626;
}

.btn-danger:hover {
  background: #b91c1c;
}
</style>
