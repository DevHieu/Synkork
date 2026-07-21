export interface Invoice {
  id: string
  amount: number
  paidAt?: string | null
  createdAt: string
  updatedAt?: string | null
  plan: string
  billingCycle?: BillingCycle | null
  status: string
  transactionId?: string | null
  paymentMethod?: string | null
  userEmail?: string | null
  username?: string | null
}

export type PlanCode = 'FREE' | 'TEAM' | 'BUSINESS'
export type PaidPlanCode = Exclude<PlanCode, 'FREE'>
export type BillingCycle = 'MONTHLY' | 'YEARLY'
export type SubscriptionStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'PENDING'

export interface UserSubscription {
  id: string
  userId?: string | null
  username?: string | null
  userEmail?: string | null
  plan: PlanCode
  status: SubscriptionStatus
  startedAt: string
  expiresAt?: string | null
  autoRenew: boolean
  current: boolean
  invoiceId?: string | null
  invoiceAmount?: number | string | null
  invoiceStatus?: string | null
  paymentMethod?: string | null
  transactionId?: string | null
  createdAt: string
  updatedAt?: string | null
}

export interface PlanPricing {
  id: string
  plan: PaidPlanCode
  billingCycle: BillingCycle
  amount: number | string
  active: boolean
  createdAt?: string | null
}

export interface PlanPricingRequest {
  plan: PaidPlanCode
  billingCycle: BillingCycle
  amount: number
}

export interface InvoiceRequest {
  userEmail: string
  amount: number
  plan: string
  status: string
  paymentMethod: string
  orderId?: string
}

export interface InvoiceSearchParams {
  page?: number
  size?: number
  status?: string
  plan?: string
  billingCycle?: string
  paymentMethod?: string
  search?: string
  dateFrom?: string
  dateTo?: string
}

export interface SubscriptionSearchParams {
  page?: number
  size?: number
  search?: string
  plan?: string
  status?: string
  current?: boolean
  expiresFrom?: string
  expiresTo?: string
}
