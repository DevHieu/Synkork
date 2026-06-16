import { z } from 'zod'

export const invoiceSchema = z.object({
  id: z.string(),
  amount: z.number(),
  paidAt: z.string().nullable().optional(),
  createdAt: z.string(),
  updatedAt: z.string().nullable().optional(),
  plan: z.string().nullable().optional(),
  status: z.enum(['PENDING', 'PAID', 'FAILED', 'CANCELLED']).or(z.string()),
  transactionId: z.string().nullable().optional(),
  paymentMethod: z.string().nullable().optional(),
  userEmail: z.string().nullable().optional(),
  username: z.string().nullable().optional(),
})

export type Invoice = z.infer<typeof invoiceSchema>

export const invoiceRequestSchema = z.object({
  userEmail: z.string().email(),
  amount: z.number(),
  plan: z.string(),
  status: z.enum(['PENDING', 'PAID', 'FAILED', 'CANCELLED']).or(z.string()),
  paymentMethod: z.string(),
  orderId: z.string().optional(),
})

export type InvoiceRequest = z.infer<typeof invoiceRequestSchema>

export const invoiceSearchResponseSchema = z.object({
  content: z.array(invoiceSchema).default([]),
  totalElements: z.number().default(0),
  totalPages: z.number().default(0),
  number: z.number().default(0),
  size: z.number().default(0),
})

export type InvoiceSearchResponse = z.infer<typeof invoiceSearchResponseSchema>

export interface InvoiceSearchParams {
  page?: number
  size?: number
  status?: string
  plan?: string
  paymentMethod?: string
  email?: string
  username?: string
  startDate?: string
  endDate?: string
}
