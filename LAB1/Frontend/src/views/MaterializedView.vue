<template>
  <div class="map-container" @click="logCoordinates">
    <div
      v-for="region in resources"
      :key="region.region_name"
      class="region-dot"
      :style="getRegionStyle(region.region_name)"
      @mouseenter="hoveredRegion = region"
      @mouseleave="hoveredRegion = null"
    >
      <div class="pulse"></div>
    </div>

    <div 
      v-if="hoveredRegion" 
      class="tooltip" 
      :style="getTooltipStyle(hoveredRegion.region_name)"
    >
      <div class="tooltip-header">
        📍 Región: {{ hoveredRegion.region_name.toUpperCase() }}
      </div>
      <div class="tooltip-body">
        <p><strong>RAM:</strong> {{ hoveredRegion.total_ram }} GB</p>
        <p><strong>CPU:</strong> {{ hoveredRegion.total_cpu }} Cores</p>
        <p><strong>Storage:</strong> {{ hoveredRegion.total_storage }} GB</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// Usamos el cliente 'api' que maneja automáticamente el token de Keycloak [cite: 2050, 2064]
import api from '@/services/http-common';

const resources = ref([]);
const hoveredRegion = ref(null);

/**
 * Diccionario de coordenadas basado en la imagen de 1357x628.
 * Se usan porcentajes para asegurar que los puntos se mantengan en su lugar
 * incluso si el contenedor cambia de tamaño.
 */
const regionCoordinates = {
  // Ajustado para la costa este de EE.UU. (Virginia/us-east)
  'us-east': { top: '29.5%', left: '7.46%' },
  // Coordenadas estimadas para otras regiones comunes
  'us-west': { top: '38%', left: '15%' },
  'sa-east': { top: '72%', left: '35%' },
  'eu-central': { top: '30%', left: '52%' },
  'ap-northeast': { top: '38%', left: '85%' }
};

const fetchResources = async () => {
  try {
    // El backend solo devuelve instancias en estado 'Running' [cite: 1809, 2162]
    const response = await api.get('/api/admin/reports/global-resources');
    resources.value = response.data;
  } catch (error) {
    console.error('Error al cargar la Vista Materializada:', error);
  }
};

const getRegionStyle = (regionName) => {
  const coords = regionCoordinates[regionName.toLowerCase()];
  if (!coords) return { display: 'none' };
  
  return {
    top: coords.top,
    left: coords.left,
  };
};

const getTooltipStyle = (regionName) => {
  const coords = regionCoordinates[regionName.toLowerCase()];
  if (!coords) return { display: 'none' };
  
  return {
    top: `calc(${coords.top} - 120px)`,
    left: `calc(${coords.left} + 25px)`,
  };
};

// Sólo para pruebas
const logCoordinates = (event) => {
  // Obtenemos las dimensiones y posición real de la imagen en pantalla
  const bounds = event.currentTarget.getBoundingClientRect();
  
  // Calculamos la posición X e Y del clic dentro de la imagen
  const x = event.clientX - bounds.left;
  const y = event.clientY - bounds.top;
  
  // Lo convertimos a porcentaje exacto
  const leftPercent = ((x / bounds.width) * 100).toFixed(2);
  const topPercent = ((y / bounds.height) * 100).toFixed(2);
  
  console.log(`Coordenadas para el clic: { top: '${topPercent}%', left: '${leftPercent}%' }`);
};

onMounted(() => {
  fetchResources();
});
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  max-width: 1357px;
  margin: 0 auto;
  aspect-ratio: 1357 / 628; /* Mantiene la proporción de la imagen original */
  
  /* Carga la imagen desde la carpeta de assets  */
  background-image: url('@/assets/world-map-detailed.png');
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.region-dot {
  position: absolute;
  width: 15px;
  height: 15px;
  background-color: #38bdf8; /* Azul cyan profesional */
  border: 2px solid white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  z-index: 10;
}

.pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background-color: rgba(56, 189, 248, 0.4);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulse-animation 2s infinite;
}

@keyframes pulse-animation {
  0% { width: 15px; height: 15px; opacity: 1; }
  100% { width: 50px; height: 50px; opacity: 0; }
}

.tooltip {
  position: absolute;
  width: 240px;
  background-color: rgba(15, 23, 42, 0.95); /* Color oscuro tipo dashboard cloud [cite: 1990] */
  color: white;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 15px;
  z-index: 100;
  pointer-events: none;
}

.tooltip-header {
  font-weight: bold;
  font-size: 14px;
  border-bottom: 1px solid #475569;
  margin-bottom: 10px;
  padding-bottom: 5px;
  color: #38bdf8;
}

.tooltip-body p {
  margin: 5px 0;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
}
</style>