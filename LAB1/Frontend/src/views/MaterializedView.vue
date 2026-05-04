<template>
  <div class="map-container">
    <div
      v-for="region in resources"
      :key="region.region_name"
      class="region-dot"
      :style="getRegionStyle(region)"
      @mouseenter="hoveredRegion = region"
      @mouseleave="hoveredRegion = null"
    >
      <div class="pulse"></div>
    </div>

    <div 
      v-if="hoveredRegion" 
      class="tooltip" 
      :style="getTooltipStyle(hoveredRegion)"
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

    <div v-if="!isLoading && resources.length === 0" class="empty-overlay">
      <p>No hay regiones con instancias activas</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// Usamos el cliente 'api' que maneja automáticamente el token de Keycloak [cite: 2050, 2064]
import api from '@/services/http-common';

const resources = ref([]);
const hoveredRegion = ref(null);
const isLoading = ref(false);

const fetchResources = async () => {
  isLoading.value = true;
  try {
    // El backend solo devuelve instancias en estado 'Running' [cite: 1809, 2162]
    const response = await api.get('/api/admin/reports/global-resources');
    resources.value = response.data;
  } catch (error) {
    console.error('Error al cargar la Vista Materializada:', error);
  } finally {
    isLoading.value = false;
  }
};

/**
 * Obtiene el estilo de posicionamiento para un punto de región.
 * Usa las coordenadas map_top y map_left que vienen directamente 
 * de la vista materializada (desde la tabla Region en la BD).
 */
const getRegionStyle = (region) => {
  if (region.map_top == null || region.map_left == null) {
    return { display: 'none' };
  }
  return {
    top: region.map_top + '%',
    left: region.map_left + '%',
  };
};

const getTooltipStyle = (region) => {
  if (region.map_top == null || region.map_left == null) {
    return { display: 'none' };
  }
  
  // Ajustar posición del tooltip para que no se salga del mapa
  const top = region.map_top;
  const left = region.map_left;
  
  // Si el punto está muy a la derecha, mostrar tooltip a la izquierda
  const tooltipLeft = left > 75 
    ? `calc(${left}% - 265px)` 
    : `calc(${left}% + 25px)`;
  
  // Si el punto está muy arriba, mostrar tooltip abajo
  const tooltipTop = top < 20 
    ? `calc(${top}% + 20px)` 
    : `calc(${top}% - 120px)`;

  return {
    top: tooltipTop,
    left: tooltipLeft,
  };
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
  
  /* Carga la imagen desde la carpeta public */
  background-image: url('/map.png');
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

.empty-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
}

.empty-overlay p {
  background: rgba(15, 23, 42, 0.9);
  color: #94a3b8;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
}
</style>