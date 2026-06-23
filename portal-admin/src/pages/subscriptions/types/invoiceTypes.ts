export interface Invoice {
  id: string
  amount: number
  paidAt?: string | null
  createdAt: string
  updatedAt?: string | null
  plan?: string | null
  status: string
  transactionId?: string | null
  paymentMethod?: string | null
  userEmail?: string | null
  username?: string | null
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
  paymentMethod?: string
  search?: string
  dateFrom?: string
  dateTo?: string
}
