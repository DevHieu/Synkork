import { BadgeHelpIcon, BellDotIcon, BirdIcon, BoxesIcon, BugIcon, ComponentIcon, CreditCardIcon, LayoutDashboardIcon, ListTodoIcon, MonitorCog, PaletteIcon, PictureInPicture2Icon, PodcastIcon, ServerIcon, SettingsIcon, ShieldAlert, SquareUserRoundIcon, UserIcon, UserRoundKey, UsersIcon, WalletCards, WrenchIcon } from '@lucide/vue'

import type { NavGroup } from '@/components/app-sidebar/types'

export function useSidebar() {
  const settingsNavItems = [
    { title: 'Profile', url: '/settings/', icon: UserIcon },
    { title: 'Account', url: '/settings/account', icon: WrenchIcon },
    { title: 'Appearance', url: '/settings/appearance', icon: PaletteIcon },
    { title: 'Notifications', url: '/settings/notifications', icon: BellDotIcon },
    { title: 'Display', url: '/settings/display', icon: PictureInPicture2Icon },
  ]

  const navData = ref<NavGroup[]> ([
    {
      title: 'General',
      items: [
        { title: 'Dashboard', url: '/dashboard', icon: LayoutDashboardIcon },
        { title: 'Users', url: '/users', icon: UsersIcon },
        { title: 'Rooms & Spaces', url: '/rooms', icon: ServerIcon },
        { title: 'Subscription', url: '/subscriptions', icon: WalletCards },
        { title: 'Report', url: '/report', icon: ShieldAlert },
      ],
    },
    {
      title: 'System',
      items: [
        { title: 'System Log', url: '/log', icon: MonitorCog },
        { title: 'Manager & Admin', url: '/manager', icon: UserRoundKey }
      ]
    },
    {
      title: 'Other',
      items: [
        { title: 'Settings', items: settingsNavItems, icon: SettingsIcon },
        { title: 'Help Center', url: '/help-center', icon: BadgeHelpIcon },
      ],
    },
  ])

  const otherPages = ref<NavGroup[]>([
    {
      title: 'Other',
      items: [
        { title: 'Plans & Pricing', icon: CreditCardIcon, url: '/billing' },
      ],
    },
  ])

  return {
    navData,
    otherPages,
    settingsNavItems,
  }
}
