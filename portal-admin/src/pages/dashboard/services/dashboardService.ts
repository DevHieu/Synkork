import axiosClient from '@/lib/axiosClient'

export const dashboardService = {
  async getOverviewStatsData() {
      const res = await axiosClient.get(`/manage/dashboard/overview/stats`)
      console.log(res);
      
      return res.data
    },

  async getOverviewChartData(period: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY') {
    const res = await axiosClient.get(`/manage/dashboard/overview/chart?period=${period}`)
      console.log(res);

    
    return res.data
  },

  async getSubscriptionDashboardData() {
    const res = await axiosClient.get(`/manage/dashboard/subscriptions`)
    return res.data
  },
}
