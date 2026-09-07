import {
  BadgeHelpIcon,
  CreditCardIcon,
  LayoutDashboardIcon,
  MonitorCog,
  ServerIcon,
  SettingsIcon,
  ShieldAlert,
  UserIcon,
  UserRoundKey,
  UsersIcon,
  WalletCards,
} from '@lucide/vue'

import type { NavGroup } from '@/components/app-sidebar/types'

export function useSidebar() {
  const settingsNavItems = [
    { title: 'Hồ sơ', url: '/settings/', icon: UserIcon },
    { title: 'Trung tâm trợ giúp', url: '/help-center', icon: BadgeHelpIcon },
  ]

  const navData = ref<NavGroup[]>([
    {
      title: 'Tổng quan',
      items: [
        { title: 'Bảng điều khiển', url: '/dashboard', icon: LayoutDashboardIcon },
        { title: 'Người dùng', url: '/users', icon: UsersIcon },
        { title: 'Phòng & Không gian', url: '/rooms', icon: ServerIcon },
        { title: 'Gói dịch vụ', url: '/subscriptions', icon: WalletCards },
        { title: 'Báo cáo', url: '/report', icon: ShieldAlert },
      ],
    },
    {
      title: 'Hệ thống',
      items: [
        { title: 'Nhật ký hệ thống', url: '/log', icon: MonitorCog },
        { title: 'Tài khoản quản trị', url: '/manager', icon: UserRoundKey },
      ],
    },
    {
      title: 'Khác',
      items: [
        { title: 'Cài đặt', items: settingsNavItems, icon: SettingsIcon },
      ],
    },
  ])

  const otherPages = ref<NavGroup[]>([
    {
      title: 'Khác',
      items: [
        { title: 'Gói & Bảng giá', icon: CreditCardIcon, url: '/billing' },
      ],
    },
  ])

  return {
    navData,
    otherPages,
    settingsNavItems,
  }
}
