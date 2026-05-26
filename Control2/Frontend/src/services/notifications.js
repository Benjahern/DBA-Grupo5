import api from './http-common.js'

export async function getNotifications() {
  return (await api.get('/api/notifications')).data
}

export async function getUnreadCount() {
  return (await api.get('/api/notifications/unread')).data.count
}

export async function markAsRead(id) {
  return (await api.put(`/api/notifications/${id}/read`)).data
}

export async function markAllAsRead() {
  await api.put('/api/notifications/read-all')
}
