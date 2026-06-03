<script setup lang="ts">
import { onMounted } from "vue";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";

gsap.registerPlugin(ScrollTrigger);

const words = "Scroll Down".split(" ");

onMounted(() => {
  gsap.to(".dauChimMatMat", {
    scrollTrigger: {
      trigger: ".dauChimSection",
      start: "top top",
      end: "bottom bottom",
      scrub: 1,
    },
    scale: 30,
    ease: "power1.inOut",
    opacity: 0,
    force3D: true,
  });

  // Nảy từng chữ stagger
  gsap.from(".scroll-word", {
    y: -10,
    opacity: 0,
    duration: 0.6,
    ease: "bounce.out",
    stagger: 0.15,
    delay: 0.3,
  });

  // Loop nảy nhẹ mãi mãi
  gsap.to(".scroll-word", {
    y: -6,
    duration: 0.8,
    ease: "sine.inOut",
    yoyo: true,
    repeat: -1,
    stagger: 0.15,
  });

  gsap.to(".scrollDown", {
    scrollTrigger: {
      trigger: ".dauChimSection",
      start: "top top",
      end: "1% top",
      scrub: 1,
    },
    opacity: 0,
    ease: "none",
  });
});
</script>

<template>
  <!-- navbar -->
  <div class="h-20 bg-transparent w-full fixed top-0 z-50">
    <div class="flex items-center justify-between h-full px-10">
      <a href="#hero">
        <img src="/assets/logo_ngang.png" alt="Synkork" class="h-16 inline-block invert" />
      </a>
      <button class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition">
        Sign In
      </button>
    </div>
  </div>
  <div class="wrapper">

    <div id="hero" class="dauChimSection h-[250vh] relative">
      <div class="sticky top-0 h-screen flex justify-center items-center overflow-hidden z-10">

        <!-- Chữ nằm trong sticky, bottom của viewport -->
        <div class="scrollDown absolute bottom-8 left-1/2 -translate-x-1/2 flex gap-3 z-20">
          <span v-for="(word, i) in words" :key="i"
            class="scroll-word inline-block text-white text-sm tracking-widest uppercase">
            {{ word }}
          </span>
        </div>

        <div class="dauChimWrapper relative" style="z-index: 2;">
          <img src="/assets/DauChimMatMat.png" alt="DauChimMatMat" class="dauChimMatMat"
            style="transform: translate(-26px, -56px)" />
        </div>
      </div>
    </div>

    <div class="h-[200vh] w-full header-children">
      <div></div>
      <h1>Hi</h1>
      <h1>Hi</h1>
      <h1>Hi</h1>
    </div>
    <div class="h-[200vh] w-full"></div>
    <div class="flex flex-col items-center my-10">
      <img src="/assets/DauChimMatMat.png" alt="syn" class="h-30 inline-block invert" />
      <h1 class="text-[18vw] leading-none text-foreground font-bold mt-5">SYNKORK</h1>
    </div>
  </div>
</template>

<style>
.dauChimWrapper {
  position: relative;
  left: -2%;
  top: -2%;
}

.dauChimSection {
  background:
    radial-gradient(circle at 50% 50%, rgb(195, 68, 31) 0%, rgb(8, 59, 59) 40%, black 70%);
}

.wrapper {
  background: black;
}

.dauChimMatMat {
  width: 35vw;
  height: auto;
  will-change: transform;
  transform-origin: 43.5% 45%;
  backface-visibility: hidden;
  filter: invert(1) drop-shadow(0 0 20px rgba(255, 255, 255, 0.5));
}
</style>