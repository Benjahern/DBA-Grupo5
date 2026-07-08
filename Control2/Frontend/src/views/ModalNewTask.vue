<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card">
      <div class="modal-header">
        <h2>Nueva tarea</h2>
        <button class="icon-btn" type="button" @click="emitClose">x</button>
      </div>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="task-title">Titulo</label>
          <input
            id="task-title"
            v-model.trim="form.title"
            type="text"
            placeholder="Titulo de la tarea"
            required
          />
        </div>

        <div class="form-group">
          <label for="task-description">Descripcion</label>
          <textarea
            id="task-description"
            v-model.trim="form.description"
            rows="3"
            placeholder="Describe la tarea"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="task-sector">Sector</label>
            <select id="task-sector" v-model="form.sectorId" required>
              <option value="" disabled>Selecciona un sector</option>
              <option v-for="sector in sectors" :key="sector.id" :value="sector.id">
                {{ sector.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="task-due-date">Fecha de vencimiento</label>
            <input
              id="task-due-date"
              v-model="form.dueDate"
              type="date"
              :min="minDate"
              required
            />
          </div>
        </div>

        <div class="map-panel">
          <div class="map-header">
            <h3>Sector seleccionado</h3>
            <span>{{ selectedSector?.name || 'Sin sector' }}</span>
          </div>

          <l-map
            ref = "mapRef"
            :zoom="mapZoom"
            :center="mapCenter"
            @click="onMapClick"
            style="height: 220px; width: 100%; border-radius: 12px;"
          >
            <l-tile-layer
              :url="tileUrl"
              :attribution="attribution"
            />
            <l-marker 
              v-if="selectedLatLng" 
              :lat-lng="selectedLatLng" 
            />
            <LMarker 
              v-if="selectedPoint" 
              :lat-lng="selectedPoint" 
            />
            <l-polygon
              v-if="selectedSectorPolygon.length > 0"
              :lat-lngs="[selectedSectorPolygon]"
              :color="'#3b82f6'"
              :fillColor="'#3b82f6'"
              :fillOpacity="0.2"
            />
          </l-map>
        </div>

        <p v-if="error" class="form-error">{{ error }}</p>

        <div class="modal-actions">
          <button class="secondary-btn" type="button" @click="emitClose">
            Cancelar
          </button>
          <button class="primary-btn" type="submit" :disabled="loading">
            {{ loading ? 'Creando...' : 'Crear tarea' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import api from '../services/http-common.js';
import * as turf from '@turf/turf';
import L from 'leaflet';
import { LMap, LMarker, LTileLayer, LPolygon } from '@vue-leaflet/vue-leaflet';
import 'leaflet/dist/leaflet.css';
import { useAlert } from '../components/Alerts/useAlert.js';

const emit = defineEmits(['close', 'created']);

const form = ref({
  title: '',
  description: '',
  sectorId: '',
  dueDate: ''
});

const sectors = ref([]);
const loading = ref(false);
const warning = ref('');
const error = ref(null);
const minDate = ref('');
const { show } = useAlert();

const defaultCenter = [-33.4489, -70.6693];
const selectedPoint = ref(null);
const mapCenter = ref(defaultCenter);
const mapZoom = ref(11);
const mapRef = ref(null);
const tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const attribution = '&copy; OpenStreetMap contributors';

const selectedSector = computed(() => {
  return sectors.value.find(s => s.id === form.value.sectorId);
});

const selectedSectorPolygon = computed(() => {
  if (!selectedSector.value?.wktGeometry) {
    return [];
  }

  const polygon = parsePolygonWKT(selectedSector.value.wktGeometry);

  return polygon;
});

const selectedLatLng = computed(() => getSectorLatLng(selectedSector.value));

watch(
  () => form.value.sectorId,
  (newId) => {
    const sector = sectors.value.find(s => s.id === newId);

    const latLng = getSectorLatLng(sector);

    if (latLng) {
      mapCenter.value = latLng;
      mapZoom.value = 14;
    }

    if (selectedSectorPolygon.value.length) {
      const bounds = L.latLngBounds(selectedSectorPolygon.value);

      if (mapRef.value?.leafletObject) {
        mapRef.value.leafletObject.fitBounds(bounds, {
          padding: [20, 20]
        });
      }
    }
  }
);

watch(() => form.value.sectorId, () => {
  selectedPoint.value = null
})

function onMapClick(e) {
  if (!selectedSector.value) {
    warning.value = 'Debes seleccionar un sector primero'
    return
  }

  const latlng = e.latlng

  // convertir punto a formato GeoJSON
  const point = turf.point([latlng.lng, latlng.lat])

  // convertir polygon a GeoJSON
  const polygon = turf.polygon([selectedSectorPolygon.value.map(p => [p[1], p[0]])]);

  const inside = turf.booleanPointInPolygon(point, polygon)

  if (!inside) {
    warning.value = 'El punto debe estar dentro del sector seleccionado'
    return
  }

  warning.value = ''

  // guardar punto válido
  selectedPoint.value = latlng
}

const emitClose = () => {
  emit('close');
};

const loadSectors = async () => {
  try {
    const response = await api.get('/api/sectors');

    sectors.value = response.data || [];
    
  } catch (err) {
    console.error('Error loading sectors:', err);
    error.value = 'No se pudieron cargar los sectores.';
    show({ message: 'No se pudieron cargar los sectores.', severity: 'error', autoHideMs: 4000 });
  }
};

const handleSubmit = async () => {
  error.value = null;
  if (!form.value.title || !form.value.dueDate || !form.value.sectorId) {
    error.value = 'Completa los campos obligatorios.';
    show({ message: 'Completa los campos obligatorios.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  if (!selectedPoint.value) {
    show({
      message: 'Debes seleccionar una ubicación para la tarea.',
      severity: 'warning',
      autoHideMs: 4000
    });
    return;
  }

  if (form.value.dueDate < minDate.value) {
    error.value = 'La fecha de vencimiento no puede ser anterior a hoy.';
    show({ message: 'La fecha de vencimiento no puede ser anterior a hoy.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  loading.value = true;
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      dueDate: form.value.dueDate,
      sectorId: Number(form.value.sectorId),
      location: {
        latitude: selectedPoint.value.lat,
        longitude: selectedPoint.value.lng
      }
    };

    const response = await api.post('/api/task', payload);

    show({ message: 'Tarea creada correctamente.', severity: 'success', autoHideMs: 3000 });
    console.log("CREATE RESPONSE:", response.data);
    emit('created', response.data.id);
    emitClose();
  } catch (err) {
    console.error('Error creating task:', err);
    error.value = 'No se pudo crear la tarea.';
    show({ message: 'No se pudo crear la tarea.', severity: 'error', autoHideMs: 4000 });
  } finally {
    loading.value = false;
  }
};

const getSectorLatLng = (sector) => {
  if (!sector) {
    return null;
  }
  if (sector.geoLocation?.coordinates?.length >= 2) {
    return [sector.geoLocation.coordinates[1], sector.geoLocation.coordinates[0]];
  }
  if (sector.geoLocation?.y != null && sector.geoLocation?.x != null) {
    return [sector.geoLocation.y, sector.geoLocation.x];
  }
  if (sector.latitude != null && sector.longitude != null) {
    return [sector.latitude, sector.longitude];
  }
  return null;
};

const parsePolygonWKT = (wkt) => {
  if (!wkt) return [];

  const cleaned = wkt
    .replace(/^POLYGON\s*\(\(/, '')
    .replace(/\)\)\s*$/, '')
    .trim();

  return cleaned
    .split(',')
    .map(point => {
      const coords = point.trim().split(/\s+/);

      if (coords.length < 2) {
        console.warn('Coordenada inválida:', coords);
        return null;
      }

      const longitude = parseFloat(coords[0]);
      const latitude = parseFloat(coords[1]);

      if (isNaN(latitude) || isNaN(longitude)) {
        console.warn('NaN detectado:', coords);
        return null;
      }

      return [latitude, longitude];
    })
    .filter(Boolean);
};

const formatLocalDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

onMounted(() => {
  if (mapRef.value?.leafletObject) {
    mapRef.value.leafletObject.on('click', onMapClick);
  }
  minDate.value = formatLocalDate(new Date());
  loadSectors();
});
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 56px 32px 32px;
  z-index: 1200;
}

.modal-card {
  width: min(88vw, 820px);
  background: white;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  overflow: auto;
  box-sizing: border-box;
  max-height: 90vh;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px 10px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.icon-btn {
  border: none;
  background: transparent;
  font-size: 1.2rem;
  cursor: pointer;
}

.modal-body {
  padding: 20px 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

input,
textarea,
select {
  width: 100%;
  padding: 11px 12px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  font-size: 0.95rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

input:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.form-error {
  color: #b91c1c;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 4px;
}

.primary-btn {
  background-color: #2563eb;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  background-color: #e2e8f0;
  color: #1f2937;
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.map-panel {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #475569;
}

.map-header h3 {
  margin: 0;
  font-size: 0.95rem;
  color: #1f2937;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .modal-backdrop {
    padding: 24px 28px;
  }
}
</style>
