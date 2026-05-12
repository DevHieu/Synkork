import { useSidebar } from '@/composables/use-sidebar'

import type { SidebarData, User } from '../types'

const user: User = {
  name: 'shadcn',
  email: 'm@example.com',
  avatar: '/avatars/shadcn.jpg',
}

const { navData } = useSidebar()

export const sidebarData: SidebarData = {
  user,
  navMain: navData.value!,
}
