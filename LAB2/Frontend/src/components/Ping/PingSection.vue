<template>
  <div class="ping-section">
    <div class="ping-header">
      <div class="ping-title">
        <span v-if="userLocation" class="ping-loc">
          <span class="loc-icon">🏙</span>
          <span class="loc-text">
            {{ userLocation.city || 'Ubicación detectada' }}<span v-if="userLocation.country">, {{ userLocation.country }}</span>
          </span>
        </span>
        <span v-else class="ping-loc muted">Sin ubicación</span>
        <button class="ping-refresh" :disabled="loading" @click="refresh" :title="loading ? 'Cargando…' : 'Re-detectar'">
          {{ loading ? '…' : '↻' }}
        </button>
      </div>
      <div class="ping-coords">
        <template v-if="userLocation">
          ({{ userLocation.latitude.toFixed(2) }}, {{ userLocation.longitude.toFixed(2) }})
        </template>
        <template v-else>&nbsp;</template>
      </div>
    </div>

    <div v-if="error" class="ping-error">{{ error }}</div>

    <div v-if="!userLocation" class="ping-manual">
      <div class="ping-manual-label">O ingresa tus coordenadas:</div>
      <div class="ping-manual-row">
        <input
          v-model.number="manualLat"
          type="number"
          step="0.0001"
          min="-90"
          max="90"
          placeholder="Latitud"
          class="ping-input"
        />
        <input
          v-model.number="manualLng"
          type="number"
          step="0.0001"
          min="-180"
          max="180"
          placeholder="Longitud"
          class="ping-input"
        />
      </div>
      <button
        class="ping-calc"
        :disabled="!isManualValid || loading"
        @click="applyManual"
      >
        Calcular
      </button>
    </div>

    <ul v-if="latencies.length" class="ping-list">
      <li
        v-for="r in latencies"
        :key="r.region_id"
        :class="['ping-item', { selected: selectedRegionId === r.region_id, nearest: isNearest(r) }]"
        @click="selectRegion(r)"
      >
        <span class="ping-item-name">
          <span v-if="isNearest(r)" class="ping-nearest-dot" title="Más cercano">●</span>
          {{ r.region_name }}
        </span>
        <span class="ping-item-latency">{{ r.latency_rtt_ms.toFixed(1) }} ms</span>
      </li>
    </ul>

    <div v-else-if="!loading && !error" class="ping-empty">
      Calculá tu latencia seleccionando una región.
    </div>

    <div v-if="selected" class="ping-detail">
      <div class="ping-detail-row">
        <span class="ping-detail-label">Región</span>
        <span class="ping-detail-value">{{ selected.region_name }}</span>
      </div>
      <div class="ping-detail-row">
        <span class="ping-detail-label">Distancia</span>
        <span class="ping-detail-value">{{ formatKm(selected.distance_m) }} km</span>
      </div>
      <div class="ping-detail-row highlight">
        <span class="ping-detail-label">Latencia RTT</span>
        <span class="ping-detail-value">{{ selected.latency_rtt_ms.toFixed(2) }} ms</span>
      </div>
      <div class="ping-detail-foot">Teórico (velocidad luz en fibra)</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { fetchUserLocation, getRegionLatencies } from '../../services/ping.js';

const userLocation = ref(null);
const latencies = ref([]);
const selectedRegionId = ref(null);
const loading = ref(false);
const error = ref(null);
const manualLat = ref(null);
const manualLng = ref(null);

const isManualValid = computed(() => {
  return (
    typeof manualLat.value === 'number' && !Number.isNaN(manualLat.value) &&
    typeof manualLng.value === 'number' && !Number.isNaN(manualLng.value) &&
    manualLat.value >= -90 && manualLat.value <= 90 &&
    manualLng.value >= -180 && manualLng.value <= 180
  );
});

const selected = computed(() =>
  latencies.value.find(r => r.region_id === selectedRegionId.value) || null
);

const nearestId = computed(() => {
  if (!latencies.value.length) return null;
  return latencies.value.reduce((a, b) =>
    a.latency_rtt_ms <= b.latency_rtt_ms ? a : b
  ).region_id;
});

const isNearest = (r) => r.region_id === nearestId.value;

const formatKm = (m) => (m / 1000).toFixed(0);

const selectRegion = (r) => {
  selectedRegionId.value = r.region_id;
};

const fetchLatencies = async (lat, lng) => {
  loading.value = true;
  try {
    latencies.value = await getRegionLatencies(lat, lng);
    if (selectedRegionId.value == null && latencies.value.length) {
      selectedRegionId.value = nearestId.value;
    }
  } catch (e) {
    error.value = 'No se pudo calcular la latencia.';
    latencies.value = [];
  } finally {
    loading.value = false;
  }
};

const applyManual = async () => {
  if (!isManualValid.value) return;
  userLocation.value = {
    latitude: manualLat.value,
    longitude: manualLng.value,
    city: 'Manual',
    country: '',
  };
  error.value = null;
  await fetchLatencies(manualLat.value, manualLng.value);
};

const refresh = async () => {
  error.value = null;
  loading.value = true;
  userLocation.value = null;
  latencies.value = [];
  const loc = await fetchUserLocation();
  if (loc) {
    userLocation.value = loc;
    await fetchLatencies(loc.latitude, loc.longitude);
  } else {
    error.value = 'No se pudo detectar tu IP. Ingresá las coordenadas manualmente.';
    loading.value = false;
  }
};

onMounted(refresh);
</script>

<style scoped>
.ping-section {
  padding: 12px 14px 16px;
  font-size: 0.85rem;
  color: #ecf0f1;
}

.ping-header {
  margin-bottom: 10px;
}

.ping-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.ping-loc {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 0.9rem;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ping-loc.muted {
  font-weight: 400;
  opacity: 0.7;
}

.loc-icon { font-size: 0.95rem; }
.loc-text { overflow: hidden; text-overflow: ellipsis; }

.ping-refresh {
  background: transparent;
  border: 1px solid #1abc9c;
  color: #1abc9c;
  border-radius: 4px;
  width: 26px;
  height: 26px;
  cursor: pointer;
  font-size: 0.95rem;
  line-height: 1;
  flex-shrink: 0;
  padding: 0;
}
.ping-refresh:disabled { opacity: 0.5; cursor: not-allowed; }
.ping-refresh:hover:not(:disabled) { background: #1abc9c; color: #1a252f; }

.ping-coords {
  font-size: 0.75rem;
  opacity: 0.6;
  margin-top: 2px;
  font-family: monospace;
}

.ping-error {
  background: rgba(231, 76, 60, 0.15);
  border: 1px solid rgba(231, 76, 60, 0.4);
  color: #f5b7b1;
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 0.78rem;
  margin-bottom: 8px;
}

.ping-manual { margin-bottom: 10px; }
.ping-manual-label { font-size: 0.75rem; opacity: 0.7; margin-bottom: 4px; }
.ping-manual-row { display: flex; gap: 6px; margin-bottom: 6px; }
.ping-input {
  flex: 1;
  background: #34495e;
  border: 1px solid #4a627a;
  color: #ecf0f1;
  padding: 4px 6px;
  border-radius: 3px;
  font-size: 0.8rem;
  min-width: 0;
}
.ping-input:focus { outline: none; border-color: #1abc9c; }
.ping-calc {
  width: 100%;
  background: #1abc9c;
  color: #1a252f;
  border: none;
  padding: 5px;
  border-radius: 3px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
}
.ping-calc:disabled { opacity: 0.5; cursor: not-allowed; }

.ping-list { list-style: none; padding: 0; margin: 8px 0; }
.ping-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 7px 8px;
  margin-bottom: 3px;
  background: #1a252f;
  border-radius: 3px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s;
}
.ping-item:hover { background: #2c3e50; }
.ping-item.selected { border-color: #1abc9c; }
.ping-item.nearest { background: rgba(26, 188, 156, 0.12); }
.ping-item-name { display: flex; align-items: center; gap: 6px; }
.ping-nearest-dot { color: #1abc9c; font-size: 0.7rem; }
.ping-item-latency {
  font-family: monospace;
  font-size: 0.8rem;
  font-weight: 600;
  color: #1abc9c;
}

.ping-empty {
  text-align: center;
  font-size: 0.78rem;
  opacity: 0.6;
  padding: 12px 4px;
}

.ping-detail {
  margin-top: 10px;
  padding: 10px;
  background: #1a252f;
  border-radius: 4px;
  border-left: 3px solid #1abc9c;
}
.ping-detail-row {
  display: flex;
  justify-content: space-between;
  padding: 3px 0;
  font-size: 0.8rem;
}
.ping-detail-row.highlight { font-size: 0.95rem; font-weight: 600; }
.ping-detail-label { opacity: 0.7; }
.ping-detail-value { font-family: monospace; }
.ping-detail-foot {
  margin-top: 6px;
  font-size: 0.7rem;
  opacity: 0.5;
  text-align: right;
  font-style: italic;
}
</style>
