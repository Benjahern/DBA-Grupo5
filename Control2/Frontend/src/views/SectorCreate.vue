<template>
  <div class="sector-create-view">
    <div class="map-section">
      <div id="map"></div>
    </div>

    <div class="sidebar">
      <div class="sidebar-header">
        <h1>Crear Sectores</h1>
        <p>Dibuja polígonos en el mapa y asígnales un nombre para guardarlos.</p>
      </div>

      <div class="controls-container">
        <input 
          v-model="newSectorName" 
          placeholder="Nombre del nuevo sector" 
          class="input-name" 
        />
        <button 
          @click="saveSector" 
          :disabled="sectors.length === 0 || !newSectorName" 
          class="save-btn"
        >
          Guardar Sector en Base de Datos
        </button>
      </div>

      <div class="sectors-list">
        <div v-if="sectors.length === 0" class="empty-state">
          Aún no has dibujado ningún sector.
        </div>

        <div v-for="sector in sectors" :key="sector.id" class="sector-card">
          <div class="sector-card-header">
            <h3>Sector {{ sector.id }}</h3>
            <span>{{ sector.points.length }} puntos</span>
          </div>

          <div class="coordinates">
            <div
              v-for="(point, index) in sector.points"
              :key="index"
              class="coordinate-item"
            >
              {{ point[0].toFixed(5) }}, {{ point[1].toFixed(5) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import api from '@/services/http-common.js' // Asegúrate de que esta ruta sea correcta

// Importamos Leaflet Draw nativo desde node_modules
import 'leaflet-draw'
import 'leaflet-draw/dist/leaflet.draw.css'

const sectors = ref([])
const newSectorName = ref('')

onMounted(() => {
  const mapInstance = L.map('map').setView([-33.5984, -70.5758], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(mapInstance)

  const drawnItems = new L.FeatureGroup()
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

    sectors.value.push({
      id: sectors.value.length + 1,
      layerId: L.stamp(layer),
      points: uniquePoints
    })

    layer.on('edit', () => { updateLayerPoints(layer) })
    layer.on('editdrag', () => { updateLayerPoints(layer) })
  })

  mapInstance.on(L.Draw.Event.EDITED, (event) => {
    event.layers.eachLayer((layer) => {
      updateLayerPoints(layer)
    })
  })

  mapInstance.on(L.Draw.Event.DELETED, (event) => {
    event.layers.eachLayer((layer) => {
      const targetId = L.stamp(layer)
      sectors.value = sectors.value.filter(s => s.layerId !== targetId)
    })
  })

  function updateLayerPoints(layer) {
    const currentId = L.stamp(layer)
    const latlngs = layer.getLatLngs()[0]
    if (!latlngs) return

    const updatedPoints = latlngs.map(point => [point.lat, point.lng])
    const sector = sectors.value.find(s => s.layerId === currentId)
    if (sector) {
      sector.points = updatedPoints
    }
  }
})

/**
 * Envía el último sector dibujado al backend
 */
const saveSector = async () => {
  if (sectors.value.length === 0 || !newSectorName.value) return

  const sectorToSave = sectors.value[sectors.value.length - 1]
  
  // Transformación al formato JSON esperado por el backend
  const payload = {
    name: newSectorName.value,
    coordinates: sectorToSave.points.map(p => ({
      latitude: p[0],
      longitude: p[1]
    }))
  }

  try {
    await api.post('/api/sectors', payload)
    alert('Sector "' + newSectorName.value + '" guardado con éxito.')
    newSectorName.value = '' // Limpiar campo
  } catch (error) {
    console.error('Error al guardar:', error)
    alert('No se pudo guardar el sector.')
  }
}
</script>

<style scoped>
.sector-create-view {
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

/* Nuevos estilos para los controles de guardado */
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

.sectors-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  color: #888;
  text-align: center;
  margin-top: 80px;
}

.sector-card {
  background-color: #242424;
  border: 1px solid #323232;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 18px;
}

.sector-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sector-card-header h3 {
  margin: 0;
  color: white;
}

.sector-card-header span {
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
</style>