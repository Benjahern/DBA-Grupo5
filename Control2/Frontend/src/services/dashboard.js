import api from './http-common.js'

export async function getTasksCountBySector() {
  return (await api.get('/api/task/my/sectors-count')).data
}

export async function getSectors() {
  return (await api.get('/api/sectors')).data
}

export async function getNearestTask(lat, lon) {
  return (await api.get(`/api/task/my/nearest?lat=${lat}&lon=${lon}`)).data
}

export async function getTopSector2Km() {
  const response = await api.get('/api/task/my/top-sector-2km')
  return response.data ? response.data : null
}

export async function getTopSector5Km() {
  const response = await api.get('/api/task/my/top-sector-5km')
  return response.data ? response.data : null
}