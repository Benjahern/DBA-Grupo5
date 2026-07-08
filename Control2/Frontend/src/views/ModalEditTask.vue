<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card">
      <div class="modal-header">
        <h2>Editar tarea</h2>
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

        <div class="form-row">
          <div class="form-group">
            <label>Estado</label>
            <span class="status" :class="statusClass(form.status)">
              {{ statusLabel(form.status) }}
            </span>
          </div>
        </div>

        <div class="map-panel">
          <div class="map-header">
            <h3>Sector seleccionado</h3>
            <span>{{ selectedSector?.name || 'Sin sector' }}</span>
          </div>

          <l-map
            ref="mapRef"
            :zoom="mapZoom"
            :center="mapCenter"
            @click="onMapClick"
            style="height: 220px; width: 100%; border-radius: 12px;"
          >
            <l-tile-layer
              :url="tileUrl"
              :attribution="attribution"
            />
            <LMarker
              v-if="taskLocation"
              :lat-lng="taskLocation"
              :icon="markerIcon"
            />
            <LMarker
              v-if="sectorCenter && !taskLocation"
              :lat-lng="sectorCenter"
              opacity="0.5"
            />
            <l-polygon
              v-if="selectedSectorPolygon.length > 0"
              :lat-lngs="[selectedSectorPolygon]"
              color="#3b82f6"
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
            {{ loading ? 'Guardando...' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick,watch } from 'vue';
import api from '../services/http-common.js';
import L from 'leaflet';
import { LMap, LMarker, LTileLayer, LPolygon } from '@vue-leaflet/vue-leaflet';
import 'leaflet/dist/leaflet.css';
import booleanPointInPolygon from '@turf/boolean-point-in-polygon';
import { point, polygon } from '@turf/helpers';
import { useAlert } from '../components/Alerts/useAlert.js';

const props = defineProps({
  task: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'updated']);

const form = ref({
  title: '',
  description: '',
  sectorId: '',
  dueDate: '',
  status: 'vigente'
});

const creationDate = ref(null);
const userId = ref(null);

const sectors = ref([]);
const loading = ref(false);
const error = ref(null);
const minDate = ref('');
const { show } = useAlert();

const mapRef = ref(null);
const taskLocation = ref(null);
const hasSelectedLocation = ref(false);
const defaultCenter = [-33.4489, -70.6693];
const tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const attribution = '&copy; OpenStreetMap contributors';

const selectedSector = computed(() =>
  sectors.value.find((sector) => sector.id === Number(form.value.sectorId))
);

const selectedSectorPolygon = computed(() => {
  const sector = sectors.value.find(
    s => s.id === Number(form.value.sectorId)
  );

  if (!sector?.wktGeometry) return [];

  return parsePolygonWKT(sector.wktGeometry);
});

const selectedLatLng = computed(() => getSectorLatLng(selectedSector.value));

const mapCenter = computed(() => {
  if (taskLocation.value) {
    return taskLocation.value;
  }

  return defaultCenter;
});

const mapZoom = computed(() => (selectedLatLng.value ? 14 : 11));

const sectorPolygonTurf = computed(() => {
  const sector = sectors.value.find(
    s => s.id === Number(form.value.sectorId)
  );

  if (!sector?.wktGeometry) return null;

  const coords = parsePolygonWKT(sector.wktGeometry);

  const converted = toTurfPolygon(coords);

  return polygon([[...converted, converted[0]]]);
});

const toTurfPolygon = (coords) => {
  return coords.map(([lat, lng]) => [lng, lat]);
};

const markerIcon = computed(() => {
  return createMarkerIcon(
    hasSelectedLocation.value ? 'red' : 'blue'
  );
});

const onMapClick = (e) => {
  const { lat, lng } = e.latlng;

  const pt = point([lng, lat]);

  const inside = booleanPointInPolygon(pt, sectorPolygonTurf.value);

  if (!inside) {
    show({
      message: 'Debes seleccionar un punto dentro del sector',
      severity: 'warning',
      autoHideMs: 3000
    });
    return;
  }

  taskLocation.value = [lat, lng];
  hasSelectedLocation.value = true;
};

const createMarkerIcon = (color = 'blue') =>
  L.divIcon({
    className: '',
    html: `
      <svg width="32" height="32" viewBox="0 0 24 24">
        <path fill="${color}" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
        <circle cx="12" cy="9" r="2.5" fill="white"/>
      </svg>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 32]
  });

const sectorCenter = computed(() => {
  const sector = selectedSector.value;
  return sector ? getSectorLatLng(sector) : null;
});

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

const statusLabel = (status) => {
  switch (status) {
    case 'VIGENTE':
      return 'Vigente';
    case 'ATRASADO':
      return 'Atrasado';
    case 'COMPLETADO':
      return 'Completado';
    case 'COMPLETADO_ATRASADO':
      return 'Completado atrasado';
    default:
      return status;
  }
};

const statusClass = (status) => {
  switch (status) {
    case 'VIGENTE':
      return 'status-vigente';

    case 'ATRASADO':
      return 'status-atrasado';

    case 'COMPLETADO':
      return 'status-completado';

    case 'COMPLETADO_ATRASADO':
      return 'status-completado-atrasado';

    default:
      return 'status-pendiente';
  }
};

const formatLocalDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const normalizeDateValue = (value) => {
  if (!value) {
    return '';
  }
  if (typeof value === 'string') {
    return value.slice(0, 10);
  }
  if (value instanceof Date) {
    return formatLocalDate(value);
  }
  return String(value).slice(0, 10);
};

const handleSubmit = async () => {
  error.value = null;
  if (!form.value.title || !form.value.dueDate || !form.value.sectorId) {
    error.value = 'Completa los campos obligatorios.';
    show({ message: 'Completa los campos obligatorios.', severity: 'warning', autoHideMs: 4000 });
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
      id: props.task.id,
      title: form.value.title,
      description: form.value.description,
      dueDate: form.value.dueDate,
      status: form.value.status,
      sectorId: Number(form.value.sectorId),
      location: taskLocation.value
        ? {
            latitude: taskLocation.value[0],
            longitude: taskLocation.value[1]
          }
        : null
    };
    await api.put('/api/task/update', payload);
    show({ message: 'Tarea actualizada correctamente.', severity: 'success', autoHideMs: 3000 });
    emit('updated');
    emitClose();
  } catch (err) {
    console.error('Error updating task:', err);
    error.value = 'No se pudo actualizar la tarea.';
    show({ message: 'No se pudo actualizar la tarea.', severity: 'error', autoHideMs: 4000 });
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

watch(
  () => props.task,
  (value) => {
    if (!value) {
      return;
    }

    form.value.title = value.title || '';
    form.value.description = value.description || '';
    form.value.sectorId = value.sectorId ?? '';
    form.value.dueDate = normalizeDateValue(value.dueDate);
    form.value.status = value.status || 'vigente';

    creationDate.value = value.creationDate || null;
    userId.value = value.userId ?? null;

    if (value.location) {
      taskLocation.value = [
        value.location.latitude,
        value.location.longitude
      ];
    } else {
      taskLocation.value = null;
    }
  },
  { immediate: true }
);

watch(
  [selectedSectorPolygon, sectors],
  async ([polygon]) => {
    if (!polygon || polygon.length === 0) return;

    await nextTick();

    const map = mapRef.value?.leafletObject;
    if (!map) return;

    const bounds = L.latLngBounds(polygon);
    map.fitBounds(bounds, { padding: [30, 30] });
  }
);

watch(
  () => form.value.sectorId,
  (newId, oldId) => {
    if (!newId || newId === oldId) return;

    taskLocation.value = null;
    hasSelectedLocation.value = false;

    show({
      message: 'Sector cambiado, selecciona nueva ubicación',
      severity: 'info',
      autoHideMs: 3000
    });
  }
);

onMounted(() => {
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
  max-height: 90vh;
  overflow-y: auto;
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

.status {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: bold;
  align-self: flex-start;
}

.status-vigente,
.status-pendiente {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.status-atrasado {
  background-color: #fee2e2;
  color: #b91c1c;
}

.status-completado {
  background-color: #dcfce7;
  color: #166534;
}

.status-completado-atrasado {
  background-color: #fef3c7;
  color: #92400e;
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
