<script setup lang="ts">
import { useRouter } from "vue-router";
import { Button } from "@/components/ui/button";

const router = useRouter();
</script>

<template>
  <div class="auth-flow-page">
    <div class="auth-flow-orb auth-flow-orb-1" />
    <div class="auth-flow-orb auth-flow-orb-2" />
    <div class="auth-flow-orb auth-flow-orb-3" />
    <div class="auth-flow-orb auth-flow-orb-4" />
    <div class="auth-flow-orb auth-flow-orb-5" />

    <section class="auth-flow-panel">
      <div class="auth-flow-brand auth-flow-brand--secondary">
        <span class="auth-flow-deco auth-flow-deco-1" />
        <span class="auth-flow-deco auth-flow-deco-2" />
        <span class="auth-flow-deco auth-flow-deco-3" />

        <img src="/assets/logo_ngang.png" alt="Logo" class="auth-flow-logo" />
        <p class="auth-flow-brand-title">Hoàn tất</p>
        <p class="auth-flow-brand-desc">
          Mật khẩu mới đã sẵn sàng. Bạn có thể quay lại đăng nhập bằng thông
          tin vừa cập nhật.
        </p>
      </div>

      <div class="auth-flow-form">
        <div class="auth-flow-inner auth-flow-success">
          <div class="auth-flow-success-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.9)"
              stroke-width="1.8">
              <path d="M20 6 9 17l-5-5" />
            </svg>
          </div>
          <h2 class="form-title">Đổi mật khẩu thành công</h2>
          <p class="auth-flow-desc">
            Tài khoản của bạn đã được cập nhật mật khẩu mới.
          </p>

          <Button class="submit-btn btn-primary" @click="router.push('/auth')">
            Đăng nhập ngay
          </Button>
        </div>
      </div>
    </section>
  </div>
</template>

<style src="@/features/auth/auth.css"></style>
