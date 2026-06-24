import axiosClient from '@/lib/axiosClient'

import type { InvoiceRequest, InvoiceSearchParams } from '../types/invoiceTypes'

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
    const res = await axiosClient.put(`/api/manage/invoices/${id}`, payload)
    return res.data
  },

  async deleteInvoice(id: string) {
    const res = await axiosClient.delete(`/api/manage/invoices/${id}`)
    return res.data
  },
}
