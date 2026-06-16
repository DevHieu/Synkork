import axiosClient from '@/lib/axiosClient'

import type { InvoiceRequest, InvoiceSearchParams } from '../types/invoiceTypes'

export const subscriptionService = {
  async getInvoices(params: { params: InvoiceSearchParams }) {
    const res = await axiosClient.get('/admin/invoices', params)
    return res.data
  },

  async getInvoiceById(id: string) {
    const res = await axiosClient.get(`/admin/invoices/${id}`)
    return res.data
  },

  async createInvoice(payload: InvoiceRequest) {
    const res = await axiosClient.post('/admin/invoices', payload)
    return res.data
  },

  async updateInvoice(id: string, payload: InvoiceRequest) {
    const res = await axiosClient.put(`/admin/invoices/${id}`, payload)
    return res.data
  },

  async deleteInvoice(id: string) {
    const res = await axiosClient.delete(`/admin/invoices/${id}`)
    return res.data
  },
}
