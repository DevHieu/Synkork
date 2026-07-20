import type { TimeRangeType } from '@/types/Date'

import axiosClient from '@/lib/axiosClient'

export const dashboardService = {
  async getOverviewStatsData() {
    const res = await axiosClient.get(`/api/manage/dashboard/overview/stats`)
    return res.data
  },

  async getOverviewChartData(period: TimeRangeType) {
    const res = await axiosClient.get(`/api/manage/dashboard/overview/chart?period=${period}`)

    return res.data
  },

  async getUserStatsData() {
    const res = await axiosClient.get(`/api/manage/dashboard/users/stats`)
    return res.data
  },

  async getRoomStatsData() {
    const res = await axiosClient.get('/api/manage/dashboard/rooms/stats')
    return res.data.data
  },

  async getRoomChartData(
    period: TimeRangeType,
  ) {
    const res = await axiosClient.get(
      `/api/manage/dashboard/rooms/chart?period=${period}`,
    )
    return res.data.data
  },

  async getSubscriptionStatData(params?: { dateFrom?: string, dateTo?: string }) {
    const res = await axiosClient.get('/api/manage/dashboard/subscriptions/stats', { params })
    return res.data
  },

  async getSubscriptionChartData(params?: { dateFrom?: string, dateTo?: string }) {
    const res = await axiosClient.get('/api/manage/dashboard/subscriptions/chart', { params })
    return res.data
  },

  async getReportStatsData() {
    const res = await axiosClient.get(`/api/manage/dashboard/reports/stats`)
    return res.data
  },

  async getReportChartData(period: TimeRangeType) {
    const res = await axiosClient.get(`/api/manage/dashboard/reports/chart?period=${period}`)
    return res.data
  },

  async getReportReasonStats() {
    const res = await axiosClient.get('/api/manage/dashboard/reports/top-reasons')
    return res.data
  },
}
