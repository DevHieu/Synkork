import { CircleDashedIcon, ClockAlertIcon, HandCoinsIcon } from '@lucide/vue'
import { h } from 'vue'

export const plans = [
  { value: 'basic', label: 'Basic' },
  { value: 'Small Business', label: 'Small Business' },
  { value: 'Enterprise', label: 'Enterprise' },
]

export const statuses = [
  { value: 'paid', label: 'Paid', icon: h(HandCoinsIcon), color: 'green' },
  { value: 'pending', label: 'Pending', icon: h(CircleDashedIcon), color: 'orange' },
  { value: 'failed', label: 'Failed', icon: h(ClockAlertIcon), color: 'red' },
]

export type PayState = 'paid' | 'pending' | 'failed'
