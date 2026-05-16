<script setup lang="ts">
import { ref } from "vue";
import AuthOverlay from "@/components/auth/AuthOverlay.vue";
import LoginForm from "@/components/auth/LoginForm.vue";
import RegisterForm from "@/components/auth/RegisterForm.vue";

const isRegister = ref(false);

const toggle = () => (isRegister.value = !isRegister.value);
const goToLogin = () => (isRegister.value = false);
</script>

<template>
  <div class="page-wrap" :class="isRegister ? 'mode-register' : 'mode-login'">
    <!-- orbs -->
    <div class="orb orb-1" />
    <div class="orb orb-2" />
    <div class="orb orb-3" />
    <div class="orb orb-4" />
    <div class="orb orb-5" />

    <div class="auth-card" :class="{ 'show-register': isRegister }">
      <AuthOverlay :is-register="isRegister" @toggle="toggle" />

      <div class="half half-login">
        <LoginForm />
      </div>

      <div class="half half-register">
        <RegisterForm @back-to-login="goToLogin" />
      </div>
    </div>
  </div>
</template>
<style src="@/components/auth/auth.css"></style>

<style scoped>
.page-wrap {
  position: relative;
  min-height: 100svh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  overflow: hidden;
  background: var(--auth-background);
  transition: background 1.2s ease;
}
.page-wrap.mode-login {
  background: var(--auth-login-background);
}
.page-wrap.mode-register {
  background: var(--auth-register-background);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  pointer-events: none;
  animation: drift 18s ease-in-out infinite;
}
.orb-1 {
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, #c8521e, transparent 70%);
  top: -15%;
  left: -10%;
  animation-duration: 20s;
}
.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #e06535, transparent 70%);
  bottom: -10%;
  right: -5%;
  animation-duration: 25s;
  animation-delay: -6s;
}
.orb-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #1a5c56, transparent 70%);
  top: 30%;
  right: 10%;
  animation-duration: 22s;
  animation-delay: -10s;
}
.orb-4 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, #6366f1, transparent 70%);
  bottom: 15%;
  left: 20%;
  opacity: 0.35;
  animation-duration: 28s;
  animation-delay: -4s;
}
.orb-5 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #f43f5e, transparent 70%);
  top: 10%;
  right: 30%;
  opacity: 0.3;
  animation-duration: 16s;
  animation-delay: -12s;
}

.mode-register .orb-1 {
  transform: translate(30%, 40%);
}
.mode-register .orb-2 {
  transform: translate(-30%, -30%);
}
.mode-register .orb-3 {
  transform: translate(-20%, 20%);
  opacity: 0.7;
}

@keyframes drift {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(3%, -4%) scale(1.04);
  }
  66% {
    transform: translate(-2%, 3%) scale(0.97);
  }
}

.auth-card {
  position: relative;
  width: 100%;
  max-width: 800px;
  height: 520px;
  border-radius: 24px;
  background: transparent;
  box-shadow:
    0 0 0 1px var(--border),
    0 24px 80px color-mix(in oklch, var(--background) 55%, transparent);
  display: flex;
  overflow: hidden;
  z-index: 1;
}

.half {
  width: 50%;
  height: 100%;
  padding: 2rem 2.6rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  overflow-y: auto;
  scrollbar-width: none;
  background: var(--auth-half-opacity);
  backdrop-filter: blur(32px) saturate(1.5);
  -webkit-backdrop-filter: blur(32px) saturate(1.5);
  transition:
    opacity var(--dur) var(--ease),
    transform var(--dur) var(--ease);
}
.half::-webkit-scrollbar {
  display: none;
}

.half-login {
  transform: translateX(0);
  opacity: 1;
}
.half-register {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  transform: translateX(40px);
  opacity: 0;
  pointer-events: none;
}

.auth-card.show-register .half-login {
  transform: translateX(-40px);
  opacity: 0;
  pointer-events: none;
}
.auth-card.show-register .half-register {
  transform: translateX(0);
  opacity: 1;
  pointer-events: all;
}
</style>
