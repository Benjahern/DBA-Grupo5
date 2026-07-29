<template>
  <div class="region-create-view">
    <div class="map-section">
      <div id="map"></div>
    </div>

    <div class="sidebar">
      <div class="sidebar-header">
        <h1>Crear Regiones</h1>
        <p>Dibuja polígonos en el mapa y asígnales un nombre para guardarlos.</p>
      </div>

      <div class="controls-container">
        <input
          v-model="newRegionName"
          placeholder="Nombre de la nueva región"
          class="input-name"
        />
        <button
          @click="saveRegion"
          :disabled="regions.length === 0 || !newRegionName"
          class="save-btn"
        >
          Guardar Región en Base de Datos
        </button>
      </div>

      <div v-if="selectedExistingRegion" class="selected-region-panel">
        <div class="panel-header">
          <h3>Región Seleccionada</h3>
          <button class="close-panel-btn" @click="selectedExistingRegion = null">✖</button>
        </div>
        <p class="selected-region-name">{{ selectedExistingRegion.Name }}</p>
        <div class="panel-actions">
          <button class="secondary-btn" @click="showEditModal = true">Editar Nombre</button>
          <button class="danger-btn" @click="showDeleteModal = true">Eliminar</button>
        </div>
      </div>

      <div class="regions-list">
        <div v-if="regions.length === 0" class="empty-state">
          Aún no has dibujado ninguna región nueva.
        </div>

        <div v-for="region in regions" :key="region.id" class="region-card">
          <div class="region-card-header">
            <h3>Nueva Región (Sin guardar)</h3>
            <span>{{ region.points.length }} puntos</span>
          </div>

          <div class="coordinates">
            <div
              v-for="(point, index) in region.points"
              :key="index"
              class="coordinate-item"
            >
              {{ point[0].toFixed(5) }}, {{ point[1].toFixed(5) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modales -->
    <ModalEditRegion
      v-if="showEditModal"
      :region="selectedExistingRegion"
      @close="showEditModal = false"
      @updated="handleRegionUpdated"
    />

    <ModalDeleteRegion
      v-if="showDeleteModal"
      :region="selectedExistingRegion"
      @close="showDeleteModal = false"
      @deleted="handleRegionDeleted"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import api from '@/services/http-common.js'
import 'leaflet-draw'
import 'leaflet-draw/dist/leaflet.draw.css'
import ModalEditRegion from '../components/Regions/ModalEditRegion.vue'
import ModalDeleteRegion from '../components/Regions/ModalDeleteRegion.vue'
import { useAlert } from '../components/Alerts/useAlert.js'

const { show } = useAlert()

const regions = ref([])
const newRegionName = ref('')
const selectedExistingRegion = ref(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const dbRegionsMap = new Map()
let drawnItems = null

onMounted(() => {
  const mapInstance = L.map('map').setView([-33.5984, -70.5758], 3)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(mapInstance)

  drawnItems = new L.FeatureGroup()
  mapInstance.addLayer(drawnItems)

  const drawControl = new L.Control.Draw({
    draw: {
      polygon: {
        allowIntersection: false,
        showArea: true,
        guidelineDistance: 10,
        shapeOptions: {
          clickable: true,
          color: '#9333ea'
        }
      },
      polyline: false,
      rectangle: false,
      circle: false,
      marker: false,
      circlemarker: false
    },
    edit: {
      featureGroup: drawnItems
    }
  })
  mapInstance.addControl(drawControl)

  mapInstance.on(L.Draw.Event.CREATED, (event) => {
    const layer = event.layer
    drawnItems.addLayer(layer)

    const latlngs = layer.getLatLngs()[0]
    const uniquePoints = latlngs.map(point => [point.lat, point.lng])

    regions.value.push({
      id: regions.value.length + 1,
      layerId: L.stamp(layer),
      points: uniquePoints
    })

    layer.on('edit', () => { updateLayerPoints(layer) })
    layer.on('editdrag', () => { updateLayerPoints(layer) })
  })

  mapInstance.on(L.Draw.Event.EDITED, (event) => {
    event.layers.eachLayer(async (layer) => {
      updateLayerPoints(layer)

      const targetId = L.stamp(layer)
      if (dbRegionsMap.has(targetId)) {
        const region = dbRegionsMap.get(targetId)
        const latlngs = layer.getLatLngs()[0]
        const updatedPoints = latlngs.map(point => ({
          latitude: point.lat,
          longitude: point.lng
        }))

        try {
          await api.put(`/api/regions/${region.region_id}`, {
            Name: region.Name,
            coordinates: updatedPoints
          })
          show({ message: 'Forma de la región actualizada en BD', severity: 'success', autoHideMs: 3000 })
        } catch (err) {
          console.error('No se pudo actualizar la forma', err)
          show({ message: 'Error al actualizar la forma de la región.', severity: 'error', autoHideMs: 5000 })
          if (window._reloadRegionsMap) window._reloadRegionsMap()
        }
      }
    })
  })

  mapInstance.on(L.Draw.Event.DELETED, (event) => {
    event.layers.eachLayer(async (layer) => {
      const targetId = L.stamp(layer)

      regions.value = regions.value.filter(r => r.layerId !== targetId)

      if (dbRegionsMap.has(targetId)) {
        const region = dbRegionsMap.get(targetId)
        try {
          await api.delete(`/api/regions/${region.region_id}`)
          dbRegionsMap.delete(targetId)
          if (selectedExistingRegion.value && selectedExistingRegion.value.region_id === region.region_id) {
            selectedExistingRegion.value = null
          }
          show({ message: 'Región eliminada correctamente', severity: 'success', autoHideMs: 3000 })
        } catch (err) {
          console.error('Error al borrar desde el mapa', err)
          let errorMessage = 'No se pudo borrar la región desde el mapa.'
          if (err.response && err.response.data) {
            errorMessage = typeof err.response.data === 'string' ? err.response.data : errorMessage
          } else {
            errorMessage = 'No se pudo borrar la región. Es posible que tenga instancias asociadas.'
          }
          show({ message: errorMessage, severity: 'error', autoHideMs: 5000 })
          if (window._reloadRegionsMap) window._reloadRegionsMap()
        }
      }
    })
  })

  function updateLayerPoints(layer) {
    const currentId = L.stamp(layer)
    const latlngs = layer.getLatLngs()[0]
    if (!latlngs) return

    const updatedPoints = latlngs.map(point => [point.lat, point.lng])
    const region = regions.value.find(r => r.layerId === currentId)
    if (region) {
      region.points = updatedPoints
    }
  }

  // Backend returns coordinates as double[][] of [lng, lat]. Flip to [lat, lng] for Leaflet.
  const parseRegionCoords = (coords) => {
    if (!coords || coords.length === 0) return []
    return coords.map(([lng, lat]) => [lat, lng])
  }

  const loadExistingRegions = async () => {
    try {
      const response = await api.get('/api/regions')
      const existingRegions = response.data || []

      dbRegionsMap.forEach((region, layerId) => {
        const layer = drawnItems.getLayer(layerId)
        if (layer) drawnItems.removeLayer(layer)
      })
      dbRegionsMap.clear()

      existingRegions.forEach(region => {
        const coords = parseRegionCoords(region.coordinates)
        if (coords.length > 0) {
          const polygon = L.polygon(coords, {
            color: '#2563eb',
            fillColor: '#3b82f6',
            fillOpacity: 0.4,
            weight: 2,
            interactive: true
          }).addTo(drawnItems)

          polygon.bindTooltip(region.Name, {
            permanent: true,
            direction: 'center',
            className: 'region-tooltip',
            opacity: 0.8
          })

          polygon.on('click', () => {
            if (selectedExistingRegion.value && selectedExistingRegion.value.region_id === region.region_id) {
              selectedExistingRegion.value = null
            } else {
              selectedExistingRegion.value = region
            }
          })

          dbRegionsMap.set(L.stamp(polygon), region)
        }
      })
    } catch (err) {
      console.error('Error fetching existing regions:', err)
    }
  }

  loadExistingRegions()
  window._reloadRegionsMap = loadExistingRegions
})

const handleRegionUpdated = () => {
  if (window._reloadRegionsMap) window._reloadRegionsMap()
  selectedExistingRegion.value = null
}

const handleRegionDeleted = () => {
  if (window._reloadRegionsMap) window._reloadRegionsMap()
  selectedExistingRegion.value = null
}

const saveRegion = async () => {
  if (regions.value.length === 0 || !newRegionName.value) return

  const regionToSave = regions.value[regions.value.length - 1]

  const payload = {
    Name: newRegionName.value,
    coordinates: regionToSave.points.map(p => ({
      latitude: p[0],
      longitude: p[1]
    }))
  }

  try {
    await api.post('/api/regions', payload)
    show({ message: 'Región "' + newRegionName.value + '" guardada con éxito.', severity: 'success', autoHideMs: 3000 })

    setTimeout(() => {
      window.location.reload()
    }, 1000)

    newRegionName.value = ''
  } catch (error) {
    console.error('Error al guardar:', error)
    show({ message: 'No se pudo guardar la región.', severity: 'error', autoHideMs: 5000 })
  }
}
</script>

<style scoped>
.region-create-view {
  display: flex;
  height: 100vh;
  background-color: #141414;
}

.map-section {
  flex: 1;
  position: relative;
  user-select: none;
}

#map {
  width: 100%;
  height: 100%;
}

.sidebar {
  width: 420px;
  background-color: #1b1b1b;
  border-left: 1px solid #2c2c2c;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid #2c2c2c;
}

.sidebar-header h1 {
  margin: 0;
  color: white;
  font-size: 28px;
}

.sidebar-header p {
  margin-top: 10px;
  color: #a3a3a3;
  line-height: 1.5;
}

.controls-container {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-bottom: 1px solid #2c2c2c;
}

.input-name {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #323232;
  background: #1a1a1a;
  color: white;
  font-size: 14px;
}

.save-btn {
  padding: 12px;
  background-color: #8b5cf6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.save-btn:hover:not(:disabled) {
  background-color: #7c3aed;
}

.save-btn:disabled {
  background-color: #374151;
  cursor: not-allowed;
  opacity: 0.6;
}

.selected-region-panel {
  padding: 20px 24px;
  background-color: #2a2a2a;
  border-bottom: 1px solid #2c2c2c;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  color: #a78bfa;
  font-size: 15px;
}

.close-panel-btn {
  background: transparent;
  border: none;
  color: #9a9a9a;
  cursor: pointer;
  font-size: 14px;
}

.selected-region-name {
  color: white;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.panel-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.secondary-btn {
  background-color: #4b5563;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  flex: 1;
}

.danger-btn {
  background-color: #dc2626;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  flex: 1;
}

.regions-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  color: #888;
  text-align: center;
  margin-top: 80px;
}

.region-card {
  background-color: #242424;
  border: 1px solid #323232;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 18px;
}

.region-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.region-card-header h3 {
  margin: 0;
  color: white;
}

.region-card-header span {
  color: #9a9a9a;
  font-size: 13px;
}

.coordinates {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coordinate-item {
  background-color: #1a1a1a;
  border-radius: 10px;
  padding: 10px;
  color: #d1d1d1;
  font-size: 13px;
  font-family: monospace;
}

:deep(.region-tooltip) {
  background-color: rgba(30, 41, 59, 0.9);
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  padding: 4px 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.3);
}
</style>