import api from './http-common.js'

export async function getTasksCountBySector() {
  return (await api.get('/api/task/my/sectors-count')).data
}

export async function getSectors() {
  return (await api.get('/api/sectors')).data
}

export const getNearestTask = async () => {
  const response = await api.get('/api/task/my/closest-task')
  return response.data
}

export async function getTopSector2Km() {
  const response = await api.get('/api/task/my/top-sector-2km')
  return response.data ? response.data : null
}

export async function getTopSector5Km() {
  const response = await api.get('/api/task/my/top-sector-5km')
  return response.data ? response.data : null
}

// Promedio de distancia de tareas completadas
export async function getAverageDistanceOfCompletedTasks() {
  const response = await api.get('/api/task/my/average-distance')
  return response.data ? response.data : null
}

// Sectores con más tareas pendientes (filtrado por usuario)
export async function getSectorsWithMostPendingTasks() {
  const response = await api.get('/api/task/my/pending/by-sector')
  return response.data ? response.data : []
}

//  Tareas por cada usuario por sector (Admin)
export async function getAllUsersCompletedBySector() {
  const response = await api.get('/api/task/all-users/completed-by-sector')
  return response.data ? response.data : []
}

//  Promedio de distancia global
export async function getAverageDistanceGlobal() {
  const response = await api.get('/api/task/my/average-distance-global')
  return response.data ? response.data : null
}