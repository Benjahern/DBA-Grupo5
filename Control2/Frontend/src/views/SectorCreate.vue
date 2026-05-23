<template>
  <div class="sector-create-view">
    <div class="map-section">
      <div id="map"></div>
    </div>

    <div class="sidebar">
      <div class="sidebar-header">
        <h1>Crear Sectores</h1>
        <p>Dibuja polígonos en el mapa para crear nuevos sectores.</p>
      </div>

      <div class="sectors-list">
        <div v-if="sectors.length === 0" class="empty-state">
          Aún no has creado sectores.
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

// Importamos Leaflet Draw nativo desde node_modules
import 'leaflet-draw'
import 'leaflet-draw/dist/leaflet.draw.css'

const sectors = ref([])

onMounted(() => {
  // CONFIGURACIÓN LOCAL AISLADA: No guardamos mapInstance ni drawnItems en variables globales externas 
  // de la vista, logrando que Vue 3 no rompa la propagación de clics de Leaflet Draw modernos (v1.x)
  const mapInstance = L.map('map').setView([-33.5984, -70.5758], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(mapInstance)

  const drawnItems = new L.FeatureGroup()
  mapInstance.addLayer(drawnItems)

  const drawControl = new L.Control.Draw({
    draw: {
      polygon: {
        allowIntersection: false, // Regla espacial obligatoria para geometrías topológicas en PostGIS
        showArea: true,
        guidelineDistance: 10,
        shapeOptions: {
          clickable: true,
          color: '#9333ea' // Púrpura estilizado para tus polígonos
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

  // 1. Capturar la creación del elemento
  mapInstance.on(L.Draw.Event.CREATED, (event) => {
    const layer = event.layer
    drawnItems.addLayer(layer)

    const latlngs = layer.getLatLngs()[0]
    const uniquePoints = latlngs.map(point => [point.lat, point.lng])

    // Guardamos los datos puros en el arreglo reactivo usando L.stamp(layer)
    // para tener una llave única que no involucre Proxies de Vue sobre objetos DOM complejos
    sectors.value.push({
      id: sectors.value.length + 1,
      layerId: L.stamp(layer),
      points: uniquePoints
    })

    // Enlazar listeners para actualizar el panel de coordenadas en tiempo real durante arrastres
    layer.on('edit', () => { updateLayerPoints(layer) })
    layer.on('editdrag', () => { updateLayerPoints(layer) })
  })

  // 2. Evento cuando el usuario confirma la edición general (botón Save de la herramienta)
  mapInstance.on(L.Draw.Event.EDITED, (event) => {
    event.layers.eachLayer((layer) => {
      updateLayerPoints(layer)
    })
  })

  // 3. Evento al borrar un sector mediante la interfaz del mapa
  mapInstance.on(L.Draw.Event.DELETED, (event) => {
    event.layers.eachLayer((layer) => {
      const targetId = L.stamp(layer)
      sectors.value = sectors.value.filter(s => s.layerId !== targetId)
    })
  })

  // Función interna encargada de recalcular los puntos mutados en el panel derecho en tiempo real
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
 * Convierte coordenadas a formato WKT POLYGON (Útil para tus pruebas con PostGIS)
 */
function convertToWKT(points) {
  const coordinates = points.map(point => `${point[1]} ${point[0]}`) // long lat
  if (coordinates.length > 0) {
    coordinates.push(coordinates[0]) // PostGIS exige duplicar el primer punto al final para cerrar el anillo
  }
  return `POLYGON((${coordinates.join(', ')}))`
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