<script setup lang="ts">
import { BadgeCheckIcon, BellIcon, ChevronsUpDownIcon, CreditCardIcon, LogOutIcon, SparklesIcon, UserRoundCogIcon } from '@lucide/vue'

import { useSidebar } from '@/components/ui/sidebar'
import { Account } from '@/types/Account';

const { user } = defineProps<
  { user: Account }
>()

const { logout } = useAuth()
const { isMobile, open } = useSidebar()
</script>

<template>
  <UiSidebarMenu>
    <UiSidebarMenuItem>
      <UiDropdownMenu>
        <UiDropdownMenuTrigger as-child>
          <UiSidebarMenuButton
            size="lg"
            class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
          >
            <UiAvatar class="size-8 rounded-lg">
              <UiAvatarImage :src="user.avatarUrl ?? ''" :alt="user.displayName" />
              <UiAvatarFallback class="rounded-lg">
                CN
              </UiAvatarFallback>
            </UiAvatar>
            <div class="grid flex-1 text-sm leading-tight text-left">
              <span class="font-semibold truncate">{{ user.displayName }}</span>
              <span class="text-xs truncate">{{ user.email }}</span>
            </div>
            <ChevronsUpDownIcon class="ml-auto size-4" />
          </UiSidebarMenuButton>
        </UiDropdownMenuTrigger>
        <UiDropdownMenuContent
          class="w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg"
          :side="(isMobile || open) ? 'bottom' : 'right'"
          align="start"
          :side-offset="4"
        >
          <UiDropdownMenuLabel class="p-0 font-normal">
            <div class="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
              <UiAvatar class="size-8 rounded-lg">
                <UiAvatarImage :src="user.avatarUrl ?? ''" :alt="user.displayName" />
                <UiAvatarFallback class="rounded-lg">
                  CN
                </UiAvatarFallback>
              </UiAvatar>
              <div class="grid flex-1 text-sm leading-tight text-left">
                <span class="font-semibold truncate">{{ user.displayName }}</span>
                <span class="text-xs truncate">{{ user.email }}</span>
              </div>
            </div>
          </UiDropdownMenuLabel>

          <UiDropdownMenuSeparator />
          <UiDropdownMenuGroup>
            <UiDropdownMenuItem @click="$router.push('/billing/')">
              <SparklesIcon />
              Nâng cấp gói
            </UiDropdownMenuItem>
          </UiDropdownMenuGroup>

          <UiDropdownMenuSeparator />
          <UiDropdownMenuGroup>
            <UiDropdownMenuItem @click="$router.push('/billing?type=billing')">
              <CreditCardIcon />
              Thanh toán
            </UiDropdownMenuItem>
          </UiDropdownMenuGroup>

          <UiDropdownMenuSeparator />
          <UiDropdownMenuGroup>
            <UiDropdownMenuItem @click="$router.push('/settings/')">
              <UserRoundCogIcon />
              Hồ sơ
            </UiDropdownMenuItem>
            <UiDropdownMenuItem @click="$router.push('/settings/account')">
              <BadgeCheckIcon />
              Tài khoản
            </UiDropdownMenuItem>
            <UiDropdownMenuItem @click="$router.push('/settings/notifications')">
              <BellIcon />
              Thông báo
            </UiDropdownMenuItem>
          </UiDropdownMenuGroup>

          <UiDropdownMenuSeparator />
          <UiDropdownMenuItem @click="logout">
            <LogOutIcon />
            {{ $t('logout') }}
          </UiDropdownMenuItem>
        </UiDropdownMenuContent>
      </UiDropdownMenu>
    </UiSidebarMenuItem>
  </UiSidebarMenu>
</template>
