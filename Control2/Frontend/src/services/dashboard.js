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