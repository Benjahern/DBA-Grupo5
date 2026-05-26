<template>
  <div class="dashboard">

    <div class="header">
      <h1>Dashboard</h1>
      <p>Resumen de tus tareas</p>
    </div>

    <div v-if="loading" class="loading">Cargando...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else>

      <!-- Nearest Task Section -->
      <div class="section">
        <h2 class="section-title">📍 Tarea más cercana</h2>
        <div v-if="nearestTask" class="nearest-card">
          <div class="nearest-info">
            <h3>{{ nearestTask.title }}</h3>
            <p>{{ nearestTask.sector?.name || 'Sin sector' }}</p>
            <span class="nearest-distance">{{ formatDistance(nearestTask.distance) }}</span>
          </div>
          <div class="nearest-actions">
            <span :class="statusClass(nearestTask.status)">{{ statusLabel(nearestTask.status) }}</span>
          </div>
        </div>
        <div v-else-if="locationError" class="empty-state">
          No se pudo obtener tu ubicación. Permite el acceso para ver la tarea más cercana.
        </div>
        <div v-else class="empty-state">
          No tienes tareas pendientes
        </div>
      </div>

      <!-- Tasks by Sector Section -->
      <div class="section">
        <h2 class="section-title">📊 Tareas por sector</h2>
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

    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getTasksCountBySector, getSectors, getNearestTask } from '../services/dashboard.js'

const sectorCounts = ref({})
const sectors = ref([])
const nearestTask = ref(null)
const loading = ref(true)
const error = ref(null)
const locationError = ref(false)

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
    const [countsData, sectorsData] = await Promise.all([
      getTasksCountBySector(),
      getSectors()
    ])
    sectorCounts.value = countsData
    sectors.value = sectorsData
  } catch (err) {
    console.error('Error fetching dashboard data:', err)
    error.value = 'No se pudieron cargar los datos.'
  } finally {
    loading.value = false
  }
}

const fetchNearestTask = () => {
  if (!navigator.geolocation) {
    locationError.value = true
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const lat = position.coords.latitude
      const lon = position.coords.longitude

      try {
        const task = await getNearestTask(lat, lon)
        if (task) {
          task.distance = calculateDistanceSimple(
            lat, lon,
            task.sector?.coordinates?.[1] || 0,
            task.sector?.coordinates?.[0] || 0
          )
        }
        nearestTask.value = task
      } catch (err) {
        console.error('Error fetching nearest task:', err)
      }
    },
    () => {
      locationError.value = true
    }
  )
}

const calculateDistanceSimple = (lat1, lon1, lat2, lon2) => {
  const R = 6371000
  const dLat = Math.toRadians(lat2 - lat1)
  const dLon = Math.toRadians(lon2 - lon1)
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
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
}

.nearest-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.nearest-info p {
  margin: 0 0 8px 0;
  color: #666;
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
</style>