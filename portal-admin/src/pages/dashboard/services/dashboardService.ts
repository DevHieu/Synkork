import axiosClient from '@/lib/axiosClient'

interface DateRangeParams {
  dateFrom?: string
  dateTo?: string
}

export const dashboardService = {
  async getOverviewStatsData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/overview/stats`, { params })
    return res.data
  },

  async getOverviewChartData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/overview/chart`, { params })

    return res.data
  },

  async getUserStatsData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/users/stats`, { params })
    return res.data
  },

  async getUserChartData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/users/chart`, { params })
    return res.data
  },

  async getRoomStatsData(params?: DateRangeParams) {
    const res = await axiosClient.get('/api/manage/dashboard/rooms/stats', { params })
    return res.data.data
  },

  async getRoomChartData(
    params?: DateRangeParams,
  ) {
    const res = await axiosClient.get(
      `/api/manage/dashboard/rooms/chart`,
      { params: { ...params } },
    )
    return res.data.data
  },

  async getSubscriptionStatData(params?: DateRangeParams) {
    const res = await axiosClient.get('/api/manage/dashboard/subscriptions/stats', { params })
    return res.data
  },

  async getSubscriptionChartData(params?: DateRangeParams) {
    const res = await axiosClient.get('/api/manage/dashboard/subscriptions/chart', { params })
    return res.data
  },

  async getReportStatsData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/reports/stats`, { params })
    return res.data
  },

  async getReportChartData(params?: DateRangeParams) {
    const res = await axiosClient.get(`/api/manage/dashboard/reports/chart`, { params: { ...params } })
    return res.data
  },

  async getReportReasonStats(params?: DateRangeParams) {
    const res = await axiosClient.get('/api/manage/dashboard/reports/top-reasons', { params })
    return res.data
  },
}
