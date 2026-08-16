import axiosClient from "@/lib/axiosClient"

export const friendService = {
  async getFriends(userId: string) {
    const res = await axiosClient.get(`/api/friends/${userId}`)
    return res.data
  },

  async sendRequestByUsername(username: string) {
    const res = await axiosClient.post(`/api/friends/request?username=${encodeURIComponent(username)}`)
    return res.data
  },

  async cancelRequest(requestId: string) {
    await axiosClient.delete(`/api/friends/request/${requestId}`)
  },

  async acceptRequest(requestId: string) {
    const res = await axiosClient.post(`/api/friends/accept/${requestId}`)
    return res.data
  },

  async rejectRequest(requestId: string) {
    const res = await axiosClient.post(`/api/friends/reject/${requestId}`)
    return res.data
  },

  async removeFriend(userId: string, friendId: string) {
    await axiosClient.delete(`/api/friends?userId=${userId}&friendId=${friendId}`)
  },

  async getPendingRequests() {
    const res = await axiosClient.get('/api/friends/requests/pending')
    return res.data
  },

  async getSentRequests() {
    const res = await axiosClient.get('/api/friends/requests/sent')
    return res.data
  }
}
