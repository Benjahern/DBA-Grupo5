<template>
  <div class="regions-page">
    <div class="map-container">
      <div
        v-for="region in resources"
        :key="region.region_name"
        class="region-dot"
        :style="getRegionStyle(region)"
      >
        <div class="pulse"></div>
      </div>

      <div v-if="!isLoading && resources.length === 0" class="empty-overlay">
        <p>No hay regiones con instancias activas</p>
      </div>
    </div>

    <div class="resources-table">
      <h2>Consumo de Recursos por Región</h2>
      <table v-if="resources.length > 0">
        <thead>
          <tr>
            <th>Región</th>
            <th>RAM (GB)</th>
            <th>CPU (Cores)</th>
            <th>Storage (GB)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="region in resources" :key="region.region_name">
            <td class="region-name">{{ region.region_name.toUpperCase() }}</td>
            <td class="ram">{{ region.total_ram }}</td>
            <td class="cpu">{{ region.total_cpu }}</td>
            <td class="storage">{{ region.total_storage }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr>
            <td class="region-name"><strong>TOTAL</strong></td>
            <td class="ram"><strong>{{ totalRam }}</strong></td>
            <td class="cpu"><strong>{{ totalCpu }}</strong></td>
            <td class="storage"><strong>{{ totalStorage }}</strong></td>
          </tr>
        </tfoot>
      </table>
      <div v-else class="no-data">
        <p>No hay datos de consumo disponibles</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/services/http-common';

const resources = ref([]);
const isLoading = ref(false);

const totalRam = computed(() => resources.value.reduce((sum, r) => sum + (r.total_ram || 0), 0));
const totalCpu = computed(() => resources.value.reduce((sum, r) => sum + (r.total_cpu || 0), 0));
const totalStorage = computed(() => resources.value.reduce((sum, r) => sum + (r.total_storage || 0), 0));

const fetchResources = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/api/admin/reports/global-resources');
    resources.value = response.data;
  } catch (error) {
    console.error('Error al cargar la Vista Materializada:', error);
  } finally {
    isLoading.value = false;
  }
};

const getRegionStyle = (region) => {
  if (region.map_top == null || region.map_left == null) {
    return { display: 'none' };
  }
  return {
    top: region.map_top + '%',
    left: region.map_left + '%',
  };
};

onMounted(() => {
  fetchResources();
});
</script>

<style scoped>
.regions-page {
  padding: 20px;
}

.map-container {
  position: relative;
  width: 100%;
  max-width: 1357px;
  margin: 0 auto;
  aspect-ratio: 1357 / 628;
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
  background-color: #38bdf8;
  border: 2px solid white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
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

.resources-table {
  margin-top: 24px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.resources-table h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #1e293b;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

th {
  color: #64748b;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.region-name {
  color: #38bdf8;
  font-weight: 600;
}

.ram {
  color: #10b981;
  font-weight: 600;
}

.cpu {
  color: #8b5cf6;
  font-weight: 600;
}

.storage {
  color: #f59e0b;
  font-weight: 600;
}

tfoot td {
  border-bottom: none;
  border-top: 2px solid #e2e8f0;
  padding-top: 16px;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #64748b;
}
</style>
