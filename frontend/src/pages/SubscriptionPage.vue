<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useUserStore } from "@/features/users/stores/userStore";
import { createPaymentLink, getPlanPricing, type PlanPricingItem } from "@/services/subscriptionService";
import { Check, X, Sparkles, Zap, Rocket } from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { storeToRefs } from "pinia";

const userStore = useUserStore();
const { userPlan, planExpiresAt } = storeToRefs(userStore);

const loading = ref(false);
const selectedPlan = ref("");
const isYearly = ref(false);

const pricingLoading = ref(true);
const pricingList = ref<PlanPricingItem[]>([]);

// Chỉ chứa thông tin TĨNH (tên, mô tả, icon, feature) — KHÔNG chứa giá.
// Giá được lấy từ API /payment/plan-pricing để admin đổi giá là FE tự cập nhật theo,
// không cần sửa code + deploy lại mỗi lần đổi giá.
const planMeta = [
  {
    id: "FREE",
    name: "Gói Miễn Phí",
    description: "Khởi đầu tuyệt vời cho cá nhân",
    icon: Zap,
    features: [
      { text: "Tối đa 5 phòng (Rooms)", included: true },
      { text: "3 Kênh Chat", included: true },
      { text: "2 Kênh Voice", included: true },
      { text: "1 Bảng Note / Task / Calendar", included: true },
      { text: "Giới hạn file 1MB", included: true },
      { text: "Theme cơ bản", included: true },
    ],
    buttonText: "Gói Hiện Tại",
    accentColor: "var(--foreground)",
    cardClass: "bg-card/30 border-border hover:border-primary/50 shadow-sm",
    buttonClass: "bg-muted text-muted-foreground cursor-not-allowed",
  },
  {
    id: "TEAM",
    name: "Gói Team",
    description: "Dành cho nhóm nhỏ và sáng tạo",
    icon: Sparkles,
    features: [
      { text: "Tối đa 15 phòng (Rooms)", included: true },
      { text: "10 Kênh Chat", included: true },
      { text: "5 Kênh Voice", included: true },
      { text: "3 Bảng Note / Task / Calendar", included: true },
      { text: "Giới hạn file 10MB", included: true },
      { text: "Bộ Theme Pastel", included: true },
      { text: "AI tạo nhanh lịch/note/task từ tin nhắn", included: true },
      { text: "AI tóm tắt tài liệu", included: true },
    ],
    buttonText: "Nâng cấp gói Team",
    accentColor: "var(--primary)",
    cardClass: "bg-primary/5 border-primary/20 hover:border-primary/40 shadow-sm",
    buttonClass: "bg-primary hover:opacity-90 text-primary-foreground",
  },
  {
    id: "BUSINESS",
    name: "Gói Business",
    description: "Sức mạnh tối đa cho chuyên nghiệp",
    icon: Rocket,
    popular: true,
    features: [
      { text: "Tối đa 50 phòng (Rooms)", included: true },
      { text: "30 Kênh Chat", included: true },
      { text: "15 Kênh Voice", included: true },
      { text: "10 Bảng Note / Task / Calendar", included: true },
      { text: "Giới hạn file 50MB", included: true },
      { text: "Theme Ombre & Tùy chỉnh", included: true },
      { text: "AI tạo nhanh lịch/note/task từ tin nhắn", included: true },
      { text: "AI tóm tắt cuộc họp", included: true },
      { text: "Google Calendar", included: true },
    ],
    buttonText: "Lên đời Business",
    accentColor: "var(--secondary)",
    cardClass: "bg-secondary/5 border-secondary/20 hover:border-secondary/40 shadow-sm",
    buttonClass: "bg-secondary hover:opacity-90 text-secondary-foreground",
  },
];

// Tra giá theo plan + chu kỳ từ dữ liệu API. FREE mặc định 0đ (không cần có trong DB).
function findPricing(planId: string, cycle: "MONTHLY" | "YEARLY"): PlanPricingItem | undefined {
  if (planId === "FREE") return undefined;
  return pricingList.value.find(
    (p) => p.plan === planId && p.billingCycle === cycle && p.active
  );
}

function getOriginalPrice(pricing?: PlanPricingItem): number {
  return pricing ? Number(pricing.amount) : 0;
}

function getFinalPrice(pricing?: PlanPricingItem): number {
  if (!pricing) return 0;
  const finalAmount = pricing.finalAmount ?? Number(pricing.amount) - Number(pricing.discountAmount ?? 0);
  return Math.max(Number(finalAmount), 0);
}

function hasDiscount(pricing?: PlanPricingItem): boolean {
  return !!pricing && Number(pricing.discountAmount ?? 0) > 0 && getFinalPrice(pricing) < getOriginalPrice(pricing);
}

// Ghép metadata tĩnh với giá động lấy từ API — component dùng "plans" y hệt trước đây.
const plans = computed(() =>
  planMeta.map((meta) => {
    const monthlyPricing = findPricing(meta.id, "MONTHLY");
    const yearlyPricing = findPricing(meta.id, "YEARLY");

    return {
      ...meta,
      monthlyPrice: getFinalPrice(monthlyPricing),
      yearlyPrice: getFinalPrice(yearlyPricing),
      monthlyOriginalPrice: getOriginalPrice(monthlyPricing),
      yearlyOriginalPrice: getOriginalPrice(yearlyPricing),
      monthlyHasDiscount: hasDiscount(monthlyPricing),
      yearlyHasDiscount: hasDiscount(yearlyPricing),
    };
  })
);

const activePlan = computed(() => {
  if (!userPlan.value) return "FREE";
  return userPlan.value;
});

const daysUntilExpiry = computed(() => {
  if (!userPlan.value || userPlan.value === "FREE" || !planExpiresAt.value) {
    return null;
  }
  const now = new Date();
  const expiry = new Date(planExpiresAt.value);
  const diffMs = expiry.getTime() - now.getTime();
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
});

const isExpiringSoon = computed(() => {
  return daysUntilExpiry.value !== null && daysUntilExpiry.value <= 3;
});

const choosePlan = async (planId: string) => {
  if (planId === "FREE") return;

  selectedPlan.value = planId;
  loading.value = true;

  try {
    const billingCycle = isYearly.value ? "YEARLY" : "MONTHLY";
    const response = await createPaymentLink({
      plan: planId,
      billingCycle,
    });

    if (response.payUrl) {
      window.location.href = response.payUrl;
    }
  } catch (error) {
    console.error("Payment error:", error);
  } finally {
    loading.value = false;
  }
};

const renewPlan = () => {
  choosePlan(activePlan.value);
};

onMounted(async () => {
  try {
    pricingList.value = await getPlanPricing();
  } catch (error) {
    console.error("Không lấy được bảng giá:", error);
  } finally {
    pricingLoading.value = false;
  }
});
</script>

<template>
  <div
    class="min-h-screen bg-background text-foreground selection:bg-primary/30 transition-colors duration-300 overflow-x-hidden">
    <!-- Background Decor -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] opacity-15 blur-[120px] rounded-full bg-primary">
      </div>
      <div
        class="absolute -bottom-[10%] -right-[10%] w-[40%] h-[40%] opacity-15 blur-[120px] rounded-full bg-secondary">
      </div>
    </div>

    <div class="relative container mx-auto px-6 py-20 max-w-7xl">
      <div class="text-center mb-16 space-y-6">
        <h1 class="text-5xl md:text-7xl font-black tracking-tight text-foreground">
          Nâng cấp không gian
        </h1>
        <p class="text-xl text-muted-foreground max-w-2xl mx-auto font-medium">
          Chọn gói phù hợp với quy trình làm việc của bạn. Mở khóa các tính năng cao cấp, theme tùy chỉnh và tiềm năng
          không giới hạn.
        </p>

        <div class="flex flex-col items-center gap-4 pt-4">
          <div class="inline-flex p-1 bg-muted/50 backdrop-blur-sm rounded-2xl border border-border overflow-hidden">
            <button @click="isYearly = false" :class="cn(
              'px-8 py-2.5 rounded-xl text-sm font-bold transition-all duration-300',
              !isYearly ? 'bg-primary text-primary-foreground shadow-lg' : 'text-muted-foreground hover:text-foreground'
            )">
              Thanh toán Tháng
            </button>
            <button @click="isYearly = true" :class="cn(
              'px-8 py-2.5 rounded-xl text-sm font-bold transition-all duration-300 flex items-center gap-2',
              isYearly ? 'bg-primary text-primary-foreground shadow-lg' : 'text-muted-foreground hover:text-foreground'
            )">
              Thanh toán Năm
              <span class="px-1.5 py-0.5 rounded-md bg-green-500 text-white text-[10px] font-black uppercase">
                -20%
              </span>
            </button>
          </div>
          <p class="text-xs font-bold text-green-500" v-if="isYearly">
            Tiết kiệm được giá của 2 tháng khi đăng ký năm!
          </p>
        </div>
      </div>


      <!-- Expiry Warning Banner -->
      <div v-if="isExpiringSoon" class="mb-10 mx-auto max-w-2xl flex items-center gap-4 
           rounded-2xl border border-amber-400/50 bg-amber-50 
           dark:bg-amber-950/30 dark:border-amber-500/30
           px-6 py-4">
        <div
          class="flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center bg-amber-100 dark:bg-amber-900/40">
          <svg class="w-5 h-5 text-amber-700 dark:text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>

        <div class="flex-1 min-w-0">
          <p class="text-sm font-bold text-amber-900 dark:text-amber-200">
            Gói {{ activePlan }} của bạn sắp hết hạn
          </p>
          <p class="text-xs text-amber-700 dark:text-amber-400 mt-0.5">
            <template v-if="daysUntilExpiry === 0">
              Hết hạn hôm nay — gia hạn ngay để không bị gián đoạn.
            </template>
            <template v-else>
              Còn <strong>{{ daysUntilExpiry }} ngày</strong> — gia hạn để giữ toàn bộ tính năng.
            </template>
          </p>
        </div>

        <Button @click="renewPlan" :disabled="loading" class="flex-shrink-0 h-10 px-5 rounded-xl bg-amber-500 hover:bg-amber-600 
             text-white font-bold text-sm transition-all duration-200 shadow-sm">
          <span v-if="loading && selectedPlan === activePlan" class="flex items-center gap-2">
            <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
              <path class="opacity-75" fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          </span>
          <span v-else>Gia hạn ngay</span>
        </Button>
      </div>

      <div v-if="pricingLoading" class="text-center py-20 text-muted-foreground font-bold">
        Đang tải bảng giá...
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
        <div v-for="plan in plans" :key="plan.id"
          class="group relative p-8 rounded-[32px] border backdrop-blur-sm transition-all duration-500 flex flex-col min-h-[660px]"
          :class="[
            plan.cardClass,
            activePlan === plan.id ? 'ring-2 ring-primary/40' : ''
          ]">

          <!-- Active Badge -->
          <div v-if="activePlan === plan.id"
            class="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1.5 rounded-full bg-foreground text-background text-[10px] font-black uppercase tracking-widest z-10 shadow-xl">
            Đang sử dụng
          </div>

          <!-- Popular Badge -->
          <div v-if="plan.popular && activePlan !== plan.id"
            class="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1.5 rounded-full bg-primary text-primary-foreground text-[10px] font-black uppercase tracking-widest z-10 shadow-xl">
            Phổ biến nhất
          </div>

          <!-- Icon & Name -->
          <div class="mb-8">
            <div
              class="w-14 h-14 rounded-2xl flex items-center justify-center mb-6 transition-transform duration-500 group-hover:scale-110 group-hover:rotate-3"
              :style="{ backgroundColor: `color-mix(in oklch, ${plan.accentColor} 15%, transparent)` }">
              <component :is="plan.icon" class="w-7 h-7" :style="{ color: plan.accentColor }" />
            </div>
            <h3 class="text-2xl font-bold mb-2">{{ plan.name }}</h3>
            <p class="text-sm text-muted-foreground leading-relaxed">{{ plan.description }}</p>
          </div>

          <!-- Price -->
          <div class="mb-10">
            <p v-if="isYearly ? plan.yearlyHasDiscount : plan.monthlyHasDiscount"
              class="text-sm text-muted-foreground font-bold line-through mb-1">
              {{ (isYearly ? plan.yearlyOriginalPrice : plan.monthlyOriginalPrice).toLocaleString('vi-VN') }}₫
            </p>
            <div class="flex items-baseline gap-1">
              <span class="text-5xl font-black">
                {{ (isYearly ? plan.yearlyPrice : plan.monthlyPrice).toLocaleString('vi-VN') }}₫
              </span>
              <span class="text-muted-foreground font-bold">/{{ isYearly ? 'năm' : 'tháng' }}</span>
            </div>
            <p v-if="isYearly && plan.monthlyPrice > 0" class="text-xs text-green-500 font-bold mt-2">
              Thanh toán theo năm (~{{ Math.round(plan.yearlyPrice / 12).toLocaleString('vi-VN') }}₫/tháng)
            </p>
          </div>

          <!-- Features -->
          <div class="grow space-y-4 mb-10">
            <div v-for="feature in plan.features" :key="feature.text" class="flex items-start gap-3">
              <div class="mt-1">
                <Check v-if="feature.included" class="w-4 h-4 text-foreground" />
                <X v-else class="w-4 h-4 text-muted-foreground/30" />
              </div>
              <span class="text-sm font-medium transition-colors"
                :class="feature.included ? 'text-foreground/80' : 'text-muted-foreground/30'">
                {{ feature.text }}
              </span>
            </div>
          </div>

          <!-- Action Button -->
          <Button @click="choosePlan(plan.id)" :disabled="loading || activePlan === plan.id || plan.id === 'FREE'"
            class="w-full h-14 rounded-2xl font-bold text-base transition-all duration-300 shadow-md" :class="[
              activePlan === plan.id || plan.id === 'FREE' ? 'bg-muted text-muted-foreground border border-border cursor-not-allowed' : plan.buttonClass
            ]">
            <template v-if="loading && selectedPlan === plan.id">
              <span class="flex items-center gap-2">
                <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none">
                  </circle>
                  <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                  </path>
                </svg>
                Đang xử lý...
              </span>
            </template>
            <template v-else>
              {{ activePlan === plan.id ? 'Gói Hiện Tại' : plan.buttonText }}
            </template>
          </Button>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
:deep(*) {
  font-family: "Plus Jakarta Sans", sans-serif;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.95);
}

.group {
  scrollbar-width: none;
}

.group::-webkit-scrollbar {
  display: none;
}
</style>
