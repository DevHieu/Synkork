<script lang="ts" setup>
import image from '@/assets/icons/synkork-logo.png'
</script>

<template>
  <div class="loader">
    <svg width="100" height="100" viewBox="0 0 100 100">

      <defs>

        <mask id="clipping">

          <polygon
            points="0,0 100,0 100,100 0,100"
            fill="black"
          />

          <polygon
            points="25,25 75,25 50,75"
            fill="white"
          />

          <polygon
            points="50,25 75,75 25,75"
            fill="white"
          />

          <polygon
            points="35,35 65,35 50,65"
            fill="white"
          />

          <polygon
            points="35,35 65,35 50,65"
            fill="white"
          />

          <polygon
            points="35,35 65,35 50,65"
            fill="white"
          />

          <polygon
            points="35,35 65,35 50,65"
            fill="white"
          />

        </mask>

      </defs>

    </svg>

    <div class="box" />
    <img
      :src="image"
      alt="Image"
      class="dauchim"
    >
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #111;
}

.dauchim {
  position: absolute;
  inset: 0;
  object-fit: contain;
  object-position: center;
  width: 70px;
  height: 70px;
  margin: auto;
  filter: invert(1);
  opacity: 50%;
}
.loader {
  position: relative;
  box-shadow:
    0 0 25px 0 #ffbf4780,
    0 20px 50px 0 #bf4a1d80;
  border-radius: 50%;
  transform: scale(5);
  animation: colorize 6s ease-in-out infinite;
}
.loader::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(180deg, #ffbf4740, #bf4a1d80);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border-top: 1px solid #ffbf48;
  border-bottom: 1px solid #be4a1d;
  box-shadow:
    inset 0 10px 10px 0 #ffbf4780,
    inset 0 -10px 10px 0 #bf4a1d80;
}
.loader .box {
  width: 100px;
  height: 100px;
  background: linear-gradient(180deg, #ffbf48 30%, #be4a1d 70%);
  mask: url(#clipping);
  -webkit-mask: url(#clipping);
}
.loader svg {
  position: absolute;
}
.loader svg #clipping {
  filter: contrast(15);
  animation: roundness 1s linear infinite;
}
.loader svg #clipping polygon {
  filter: blur(7px);
}
.loader svg #clipping polygon:nth-child(1) {
  transform-origin: 75% 25%;
  transform: rotate(90deg);
}
.loader svg #clipping polygon:nth-child(2) {
  transform-origin: 50% 50%;
  animation: rotation 2s linear infinite reverse;
}
.loader svg #clipping polygon:nth-child(3) {
  transform-origin: 50% 60%;
  animation: rotation 2s linear infinite;
  animation-delay: -0.666s;
}
.loader svg #clipping polygon:nth-child(4) {
  transform-origin: 40% 40%;
  animation: rotation 2s linear infinite reverse;
}
.loader svg #clipping polygon:nth-child(5) {
  transform-origin: 40% 40%;
  animation: rotation 2s linear infinite reverse;
  animation-delay: -1s;
}
.loader svg #clipping polygon:nth-child(6) {
  transform-origin: 60% 40%;
  animation: rotation 2s linear infinite;
}
.loader svg #clipping polygon:nth-child(7) {
  transform-origin: 60% 40%;
  animation: rotation 2s linear infinite;
  animation-delay: -1.333s;
}
@keyframes rotation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
@keyframes roundness {
  0% {
    filter: contrast(15);
  }
  20%,
  40% {
    filter: contrast(3);
  }
  60%,
  100% {
    filter: contrast(15);
  }
}
@keyframes colorize {
  0% {
    filter: hue-rotate(0deg);
  }
  20% {
    filter: hue-rotate(-30deg);
  }
  40% {
    filter: hue-rotate(-60deg);
  }
  60% {
    filter: hue-rotate(-90deg);
  }
  80% {
    filter: hue-rotate(-45deg);
  }
  100% {
    filter: hue-rotate(0deg);
  }
}
</style>
