<template>
  <div class="dashboard">

    <div class="header">
      <h1>Dashboard</h1>
      <p>Resumen de tus tareas</p>
    </div>

    <div v-if="loading" class="loading">Cargando...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else>

      <div class="section">
        <h2 class="section-title">📍 Tarea más cercana (Pendiente)</h2>
        <div v-if="nearestTask" class="nearest-card">
          <div class="nearest-info">
            <div class="nearest-row">
              <span class="nearest-label">Tarea:</span>
              <span class="nearest-value">{{ nearestTask.title }}</span>
            </div>
            <div class="nearest-row">
              <span class="nearest-label">Sector:</span>
              <span class="nearest-value">{{ nearestTask.sectorName || 'Sin sector' }}</span>
            </div>
            <div class="nearest-row">
              <span class="nearest-label">Distancia:</span>
              <span class="nearest-distance">
                {{ nearestTask.distanceMetres != null ? formatDistance(nearestTask.distanceMetres) : 'Sin distancia' }}
              </span>
            </div>
          </div>
          <div class="nearest-actions">
            <span :class="statusClass(nearestTask.status)">{{ statusLabel(nearestTask.status) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          No tienes tareas pendientes
        </div>
      </div>

      <div class="section">
        <h2 class="section-title">🏆 Top Sectores (Tareas Completadas)</h2>
        <div class="stats-container">
          
          <div class="stat-wrapper">
            <h3 class="distance-title">Dentro de 2 kilómetros</h3>
            <div class="stat-card">
              <div class="stat-icon">🏅</div>
              <div class="stat-info" v-if="topSector2Km">
                <span class="stat-label">{{ topSector2Km.sectorName }}</span>
                <span class="stat-value"><strong>{{ topSector2Km.taskCount }}</strong> tareas completadas</span>
              </div>
              <div class="stat-info" v-else>
                <span class="stat-label">Sin datos</span>
                <span class="stat-value">0 tareas completadas</span>
              </div>
            </div>
          </div>

          <div class="stat-wrapper">
            <h3 class="distance-title">Dentro de 5 kilómetros</h3>
            <div class="stat-card">
              <div class="stat-icon">🏅</div>
              <div class="stat-info" v-if="topSector5Km">
                <span class="stat-label">{{ topSector5Km.sectorName }}</span>
                <span class="stat-value"><strong>{{ topSector5Km.taskCount }}</strong> tareas completadas</span>
              </div>
              <div class="stat-info" v-else>
                <span class="stat-label">Sin datos</span>
                <span class="stat-value">0 tareas completadas</span>
              </div>
            </div>
          </div>

        </div>
      </div>

      <div class="section">
        <h2 class="section-title">📊 Tareas Totales por sector</h2>
        <div class="stats-container">
          <div class="stat-card" v-for="item in sectorStats" :key="item.sectorId">
            <div class="stat-icon">📍</div>
            <div class="stat-info">
              <span class="stat-label">{{ item.sectorName }}</span>
              <span class="stat-value">{{ item.count }} tareas</span>
            </div>
          </div>
          <div v-if="sectorStats.length === 0" class="empty-state">
            No tienes tareas asignadas
          </div>
        </div>
      </div>

      <div class="section">
        <h2 class="section-title">📍 Sectores con más tareas pendientes</h2>
        <div class="stats-container">
          <div class="stat-card" v-for="item in pendingSectors" :key="item.sectorId">
            <div class="stat-icon">⏳</div>
            <div class="stat-info">
              <span class="stat-label">{{ item.sectorName }}</span>
              <span class="stat-value"><strong>{{ item.taskCount }}</strong> tareas pendientes</span>
            </div>
          </div>
          <div v-if="pendingSectors.length === 0" class="empty-state">
            No hay tareas pendientes
          </div>
        </div>
      </div>

      <div class="section">
        <h2 class="section-title">📏 Promedio de distancia de tareas completadas</h2>
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-icon">📍</div>
            <div class="stat-info">
              <span class="stat-label">Distancia promedio</span>
              <span class="stat-value" v-if="avgDistanceCompleted !== null">
                <strong>{{ formatDistance(avgDistanceCompleted) }}</strong>
              </span>
              <span class="stat-value" v-else>Sin datos</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <h2 class="section-title">📏 Promedio de distancia global</h2>
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-icon">🌍</div>
            <div class="stat-info">
              <span class="stat-label">Distancia promedio global</span>
              <span class="stat-value" v-if="avgDistanceGlobal !== null">
                <strong>{{ formatDistance(avgDistanceGlobal) }}</strong>
              </span>
              <span class="stat-value" v-else>Sin datos</span>
            </div>
          </div>
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getTasksCountBySector,
  getSectors,
  getNearestTask,
  getTopSector2Km,
  getTopSector5Km,
  getAverageDistanceOfCompletedTasks,
  getSectorsWithMostPendingTasks,
  getAverageDistanceGlobal
} from '../services/dashboard.js'

const sectorCounts = ref({})
const sectors = ref([])
const nearestTask = ref(null)
const topSector2Km = ref(null)
const topSector5Km = ref(null)
const pendingSectors = ref([])
const avgDistanceCompleted = ref(null)
const avgDistanceGlobal = ref(null)

const loading = ref(true)
const error = ref(null)

const sectorStats = computed(() => {
  return sectors.value
    .filter(sector => sectorCounts.value[sector.id])
    .map(sector => ({
      sectorId: sector.id,
      sectorName: sector.name,
      count: sectorCounts.value[sector.id]
    }))
})

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const [
      countsData,
      sectorsData,
      top2kmData,
      top5kmData,
      avgDistData,
      pendingSectorsData,
      avgDistGlobalData
    ] = await Promise.all([
      getTasksCountBySector(),
      getSectors(),
      getTopSector2Km().catch(() => null),
      getTopSector5Km().catch(() => null),
      getAverageDistanceOfCompletedTasks().catch(() => null),
      getSectorsWithMostPendingTasks().catch(() => null),
      getAverageDistanceGlobal().catch(() => null)
    ])
    sectorCounts.value = countsData
    sectors.value = sectorsData
    topSector2Km.value = top2kmData
    topSector5Km.value = top5kmData
    avgDistanceCompleted.value = avgDistData
    pendingSectors.value = pendingSectorsData
    avgDistanceGlobal.value = avgDistGlobalData
  } catch (err) {
    console.error('Error fetching dashboard data:', err)
    error.value = 'No se pudieron cargar los datos.'
  } finally {
    loading.value = false
  }
}

const fetchNearestTask = async () => {
  try {
    const task = await getNearestTask()
    nearestTask.value = task
  } catch (err) {
    console.error('Error fetching nearest task:', err)
  }
}

const formatDistance = (meters) => {
  if (meters >= 1000) {
    return (meters / 1000).toFixed(1) + ' km'
  }
  return Math.round(meters) + ' m'
}

const statusLabel = (status) => {
  switch (status) {
    case 'vigente': return 'Vigente'
    case 'atrasado': return 'Atrasado'
    case 'completado': return 'Completado'
    case 'completadoAtrasado': return 'Completado atrasado'
    default: return 'Pendiente'
  }
}

const statusClass = (status) => {
  switch (status) {
    case 'vigente': return 'status-vigente'
    case 'atrasado': return 'status-atrasado'
    case 'completado': return 'status-completado'
    case 'completadoAtrasado': return 'status-completado-atrasado'
    default: return 'status-pendiente'
  }
}

onMounted(() => {
  fetchData()
  fetchNearestTask()
})
</script>

<style scoped>
 .dashboard {
  padding: 32px;
 }

 .header {
  margin-bottom: 28px;
 }

 .header h1 {
  margin: 0;
  font-size: 32px;
 }

 .header p {
  color: #717070;
  margin-top: 8px;
 }

 .section {
  margin-bottom: 40px;
 }

 .section-title {
  font-size: 20px;
  margin-bottom: 16px;
  color: #333;
 }

 .stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
 }

 .stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
 }

 .stat-icon {
  font-size: 32px;
 }

 .stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
 }

 .stat-label {
  font-weight: 600;
  font-size: 16px;
  color: #333;
 }

 .stat-value {
  font-size: 14px;
  color: #666;
 }

 .nearest-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
 }

 .nearest-info {
  display: flex;
  flex-direction: row;
  flex: 1;
  flex-wrap: nowrap;
  gap: 32px;
  justify-content: space-between;
  min-width: 0;
 }

 .nearest-row {
  display: flex;
  gap: 8px;
  align-items: baseline;
  flex: 1;
  min-width: 0;
 }

 .nearest-label {
  font-weight: 600;
  color: #334155;
 }

 .nearest-value {
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
 }

 .nearest-distance {
  color: #2563eb;
  font-weight: 600;
  font-size: 14px;
 }

 .nearest-actions {
  display: flex;
  align-items: center;
  gap: 12px;
 }

 .status-vigente,
 .status-pendiente {
  background-color: #dbeafe;
  color: #1d4ed8;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
 }

 .status-atrasado {
  background-color: #fee2e2;
  color: #b91c1c;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
 }

 .status-completado {
  background-color: #dcfce7;
  color: #166534;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
 }

 .status-completado-atrasado {
  background-color: #fef3c7;
  color: #92400e;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
 }

 .loading,
 .error,
 .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
 }

 .stat-subtitle {
  font-size: 13px;
  color: #717070;
  font-weight: 500;
 }

 .stat-wrapper {
  display: flex;
  flex-direction: column;
 }

 .distance-title {
  font-size: 15px;
  color: #666;
  margin: 0 0 10px 4px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
 }


</style>