import {
  BadgeHelpIcon,
  BellDotIcon,
  CreditCardIcon,
  LayoutDashboardIcon,
  MonitorCog,
  PaletteIcon,
  PictureInPicture2Icon,
  ServerIcon,
  SettingsIcon,
  ShieldAlert,
  UserIcon,
  UserRoundKey,
  UsersIcon,
  WalletCards,
  WrenchIcon,
} from '@lucide/vue'

import type { NavGroup } from '@/components/app-sidebar/types'

export function useSidebar() {
  const settingsNavItems = [
    { title: 'Hồ sơ', url: '/settings/', icon: UserIcon },
    { title: 'Tài khoản', url: '/settings/account', icon: WrenchIcon },
    { title: 'Giao diện', url: '/settings/appearance', icon: PaletteIcon },
    { title: 'Thông báo', url: '/settings/notifications', icon: BellDotIcon },
    { title: 'Hiển thị', url: '/settings/display', icon: PictureInPicture2Icon },
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
        { title: 'Trung tâm trợ giúp', url: '/help-center', icon: BadgeHelpIcon },
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
