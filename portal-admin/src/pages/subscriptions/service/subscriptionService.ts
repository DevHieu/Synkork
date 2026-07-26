import axiosClient from '@/lib/axiosClient'

import type { InvoiceRequest, InvoiceSearchParams, PlanPricingRequest, SubscriptionSearchParams } from '../types/invoiceTypes'

export const subscriptionService = {
  async getInvoices(params: { params: InvoiceSearchParams }) {
    const res = await axiosClient.get('/api/manage/invoices', params)
    return res.data
  },

  async getInvoiceById(id: string) {
    const res = await axiosClient.get(`/api/manage/invoices/${id}`)
    return res.data
  },

  async createInvoice(payload: InvoiceRequest) {
    const res = await axiosClient.post('/api/manage/invoices', payload)
    return res.data
  },

  async updateInvoice(id: string, payload: InvoiceRequest) {
    const res = await axiosClient.patch(`/api/manage/invoices/${id}`, payload)
    return res.data
  },

  async deleteInvoice(id: string) {
    const res = await axiosClient.delete(`/api/manage/invoices/${id}`)
    return res.data
  },

  async getSubscriptions(params: { params: SubscriptionSearchParams }) {
    const res = await axiosClient.get('/api/manage/subscriptions', params)
    return res.data
  },

  async getPlanPricings() {
    const res = await axiosClient.get('/api/payment/plan-pricing')
    return res.data
  },

  async updatePlanPricing(payload: PlanPricingRequest) {
    const res = await axiosClient.put('/api/payment/plan-pricing', payload)
    return res.data
  },
}
