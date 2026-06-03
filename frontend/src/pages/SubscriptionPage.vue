<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import axios from "@/types/axios";
import { useUserStore } from "@/stores/userStore";

const route = useRoute();
const userStore = useUserStore();
const selectedPlan = ref("");
const activePlan = ref(""); // gói đang active
const loading = ref(false);
const paymentStatus = ref<"success" | "cancel" | null>(null);
const showQR = ref(false);
const qrUrl = ref("");
const countdown = ref(5);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

onMounted(async () => {
  if (!userStore.user) {
    await userStore.getUserInfo();
  }
  if (userStore.user?.currentPlan && userStore.user.currentPlan !== "FREE") {
    activePlan.value = userStore.user.currentPlan.charAt(0).toUpperCase() 
      + userStore.user.currentPlan.slice(1).toLowerCase();
  }
});

const plans = [
  {
    name: "Free",
    price: "$0",
    theme: "Màu cơ bản",
    calendar: false,
    rooms: 5,
    chat: 3,
    voice: 2,
    note: 1,
    fileSize: "1MB",
    buttonColor: "bg-white/20 hover:bg-white/30",
    cardClass: "border border-white/10 bg-white/5 backdrop-blur-md",
  },
  {
    name: "Team",
    price: "$9.99",
    theme: "Pastel",
    calendar: true,
    rooms: 10,
    chat: 10,
    voice: 5,
    note: 3,
    fileSize: "10MB",
    buttonColor: "bg-pink-500 hover:bg-pink-400",
    cardClass: "border border-pink-400/30 bg-white/10 backdrop-blur-md",
  },
  {
    name: "Business",
    price: "$19.99",
    theme: "Ombre",
    calendar: true,
    rooms: 30,
    chat: 20,
    voice: 10,
    note: 10,
    fileSize: "50MB",
    popular: true,
    buttonColor: "bg-red-600 hover:bg-red-500",
    cardClass: "border border-red-400/30 bg-white/10 backdrop-blur-md",
  },
];

const choosePlan = async (planName: string) => {
  if (planName === "Free") return;

  selectedPlan.value = planName;
  loading.value = true;

  try {
    const response = await axios.post("/payment/momo", {
      plan: planName.toUpperCase(),
    });

    qrUrl.value = response.data.paymentUrl;
    showQR.value = true;
    countdown.value = 5;

    // Mở trang MoMo tab mới
    window.open(qrUrl.value, "_blank");

    countdownTimer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(countdownTimer!);
        showQR.value = false;
        paymentStatus.value = "success";
        activePlan.value = planName; // đánh dấu gói đã mua
        selectedPlan.value = "";

        // Tự ẩn toast sau 3 giây
        setTimeout(() => {
          paymentStatus.value = null;
        }, 3000);
      }
    }, 1000);

  } catch (error) {
    console.error("Payment error:", error);
    alert("Có lỗi xảy ra, vui lòng thử lại.");
    selectedPlan.value = "";
  } finally {
    loading.value = false;
  }
};

const cancelQR = () => {
  if (countdownTimer) clearInterval(countdownTimer);
  showQR.value = false;
  selectedPlan.value = "";
  paymentStatus.value = "cancel";

  // Tự ẩn toast sau 3 giây
  setTimeout(() => {
    paymentStatus.value = null;
  }, 3000);
};
</script>

<template>
  <div class="min-h-screen background text-white px-6 py-10">

    <!-- Modal QR -->
    <div v-if="showQR"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-3xl p-8 flex flex-col items-center gap-4 max-w-sm w-full mx-4">

        <h2 class="text-xl font-bold text-gray-800">Quét mã để thanh toán</h2>
        <p class="text-sm text-gray-500">Tab MoMo đã mở — hoặc quét QR bên dưới</p>

        <img
          :src="`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrUrl)}`"
          class="w-48 h-48 rounded-xl border border-gray-100"
        />

        <div class="flex flex-col items-center gap-1">
          <div class="text-4xl font-bold text-pink-500">{{ countdown }}</div>
          <p class="text-xs text-gray-400">Tự động xác nhận sau {{ countdown }} giây</p>
        </div>

        <div class="w-full bg-gray-100 rounded-full h-1.5">
          <div
            class="bg-pink-500 h-1.5 rounded-full transition-all duration-1000"
            :style="{ width: `${((5 - countdown) / 5) * 100}%` }"
          />
        </div>

        <button
          @click="cancelQR"
          class="text-sm text-gray-400 hover:text-gray-600 mt-2 transition"
        >
          Huỷ thanh toán
        </button>
      </div>
    </div>

    <!-- Toast thông báo — fixed góc trên, tự ẩn -->
    <transition name="fade">
      <div v-if="paymentStatus === 'success'"
        class="fixed top-4 left-1/2 -translate-x-1/2 z-50 px-6 py-3 rounded-2xl bg-green-500/90 text-white text-sm font-medium shadow-lg flex items-center gap-2">
        ✅ Thanh toán thành công! Gói của bạn đã được kích hoạt.
      </div>
    </transition>
    <transition name="fade">
      <div v-if="paymentStatus === 'cancel'"
        class="fixed top-4 left-1/2 -translate-x-1/2 z-50 px-6 py-3 rounded-2xl bg-yellow-500/90 text-white text-sm font-medium shadow-lg flex items-center gap-2">
        ⚠️ Bạn đã huỷ thanh toán.
      </div>
    </transition>

    <!-- Header -->
    <div class="max-w-7xl mx-auto mb-10">
      <h1 class="text-4xl md:text-5xl font-bold mb-4">
        Choose the plan that's right for you
      </h1>
      <div class="space-y-2 text-white/70">
        <p>✔ Custom themes for your workspace</p>
        <p>✔ Upgrade your productivity with more rooms & spaces</p>
        <p>✔ Cancel or change plan anytime</p>
      </div>
    </div>

    <!-- Pricing Table -->
    <div class="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-4 gap-6">

      <!-- Features -->
      <div class="border border-white/10 bg-white/5 backdrop-blur-md rounded-3xl p-6 hidden lg:block">
        <h2 class="text-red-400 text-3xl font-bold mb-10">SYNKORK VIP</h2>
        <div class="space-y-8 text-white/70">
          <p>🎨 Theme màu</p>
          <p>📅 Google Calendar / Keep</p>
          <p>🚪 Room có thể tạo</p>
          <p>💬 Voice / Chat / Note</p>
          <p>📂 Giới hạn dung lượng file</p>
        </div>
      </div>

      <!-- Plan Cards -->
      <div
        v-for="plan in plans"
        :key="plan.name"
        class="relative rounded-3xl p-6 transition duration-300 hover:scale-105 hover:shadow-2xl"
        :class="[
          plan.cardClass,
          activePlan === plan.name ? 'ring-2 ring-green-400/60' : ''
        ]"
      >
        <!-- Popular badge -->
        <div
          v-if="plan.popular"
          class="absolute top-5 right-5 bg-red-600 text-xs px-3 py-1 rounded-full font-semibold"
        >
          Popular
        </div>

        <!-- Active badge -->
        <div
          v-if="activePlan === plan.name"
          class="absolute top-5 left-5 bg-green-500 text-xs px-3 py-1 rounded-full font-semibold"
        >
          ✅ Đang dùng
        </div>

        <div class="mb-8">
          <h2 class="text-2xl font-semibold">{{ plan.name }}</h2>
          <div class="mt-2">
            <span class="text-4xl font-bold">{{ plan.price }}</span>
            <span v-if="plan.name !== 'Free'" class="text-white/50">/month</span>
          </div>
        </div>

        <div class="space-y-8 text-center">
          <div>
            <p class="text-lg font-medium">{{ plan.theme }}</p>
          </div>
          <div>
            <span class="text-2xl" :class="plan.calendar ? 'text-green-400' : 'text-white/30'">
              {{ plan.calendar ? "✔" : "✖" }}
            </span>
          </div>
          <div class="text-xl font-semibold">{{ plan.rooms }}</div>
          <div class="text-sm text-white/60 leading-7">
            <p>{{ plan.chat }} Chat Room</p>
            <p>{{ plan.voice }} Voice Room</p>
            <p>{{ plan.note }} Note / Task</p>
          </div>
          <div class="text-xl font-semibold">{{ plan.fileSize }}</div>
        </div>

        <!-- Button -->
        <button
          @click="choosePlan(plan.name)"
          :disabled="loading || plan.name === 'Free' || activePlan === plan.name"
          class="mt-10 w-full rounded-xl py-3 font-semibold transition"
          :class="[
            plan.name === 'Free' || activePlan === plan.name
              ? 'bg-white/10 cursor-not-allowed opacity-50 text-white/60'
              : plan.buttonColor,
          ]"
        >
          <span v-if="loading && selectedPlan === plan.name">Đang xử lý...</span>
          <span v-else-if="plan.name === 'Free'">Current Plan</span>
          <span v-else-if="activePlan === plan.name">Current Plan</span>
          <span v-else>Choose Plan</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>