<script lang="ts">
import { defineComponent, h } from "vue";

const OvBrand = defineComponent({
  name: "OvBrand",
  render() {
    return h(
      "div",
      { style: "margin-bottom:20px" },
      h("svg", { width: 32, height: 32, viewBox: "0 0 32 32", fill: "none" }, [
        h("rect", {
          width: 32,
          height: 32,
          rx: 8,
          fill: "rgba(255,255,255,0.15)",
        }),
        h("path", {
          d: "M8 16a8 8 0 1 1 16 0A8 8 0 0 1 8 16Z",
          fill: "rgba(255,255,255,0.3)",
        }),
        h("circle", { cx: 16, cy: 16, r: 4, fill: "#fff" }),
      ]),
    );
  },
});

export default { components: { OvBrand } };
</script>

<script setup lang="ts">
defineProps<{ isRegister: boolean }>();
defineEmits<{ toggle: [] }>();
</script>

<template>
  <div class="overlay" :class="{ 'overlay--register': isRegister }">
    <span class="deco d1" /><span class="deco d2" /><span class="deco d3" />

    <div class="ov-panels">
      <div class="ov-panel ov-panel--left">
        <img src="/assets/logo_ngang.png" alt="Logo" class="w-70 mb-5" />
        <p class="ov-title">Chào mừng trở lại!</p>
        <p class="ov-desc">Đăng nhập để tiếp tục hành trình cùng chúng tôi</p>
        <button class="ov-btn" @click="$emit('toggle')">Đăng nhập</button>
      </div>

      <div class="ov-panel ov-panel--right">
        <img src="/assets/logo_ngang.png" alt="Logo" class="w-70 mb-5" />
        <p class="ov-title">Xin chào!</p>
        <p class="ov-desc">Tạo tài khoản và bắt đầu trải nghiệm ngay hôm nay</p>
        <button class="ov-btn" @click="$emit('toggle')">Đăng ký</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  border-radius: 24px;
  overflow: hidden;
  clip-path: inset(0 0 0 50% round 24px);
  background: linear-gradient(145deg, var(--clr-p) 0%, var(--clr-p2) 100%);
  transition:
    clip-path var(--dur) var(--ease),
    background var(--dur) ease;
}
.overlay--register {
  clip-path: inset(0 50% 0 0 round 24px);
  background: linear-gradient(145deg, var(--clr-s) 0%, var(--clr-s2) 100%);
}

/* deco */
.deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}
.d1 {
  width: 260px;
  height: 260px;
  top: -80px;
  right: -80px;
}
.d2 {
  width: 160px;
  height: 160px;
  bottom: -55px;
  left: -45px;
}
.d3 {
  width: 100px;
  height: 100px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* panels */
.ov-panels {
  position: absolute;
  inset: 0;
}

.ov-panel {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.4rem 2rem;
  text-align: center;
}

.ov-panel--right {
  right: 0;
  opacity: 1;
  transition: opacity 0.25s ease 0.25s;
}
.ov-panel--left {
  left: 0;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
}

.overlay--register .ov-panel--right {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
}
.overlay--register .ov-panel--left {
  opacity: 1;
  pointer-events: all;
  transition: opacity 0.25s ease 0.3s;
}

.ov-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
}
.ov-desc {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.6;
  margin-bottom: 24px;
  max-width: 180px;
}
.ov-btn {
  height: 38px;
  padding: 0 26px;
  border-radius: 24px;
  border: 1.5px solid rgba(255, 255, 255, 0.75);
  background: transparent;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.15s;
}
.ov-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateY(-1px);
}
.ov-btn:active {
  transform: translateY(0);
}
</style>
