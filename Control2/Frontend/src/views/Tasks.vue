<template>
  <div class="tasks-view">

    <!-- HEADER -->
    <div class="tasks-header">

      <div>
        <h1>Tareas</h1>
        <p>Administra las tareas geoespaciales del sistema</p>
      </div>

      <button class="create-task-btn" @click="openModal">
        + Nueva tarea
      </button>

    </div>

    <ModalNewTask
      v-if="showModal"
      @close="closeModal"
      @created="handleTaskCreated"
    />

    <ModalEditTask
      v-if="showEditModal && selectedTask"
      :task="selectedTask"
      @close="closeEditModal"
      @updated="handleTaskUpdated"
    />

    <ModalDeleteTask
      v-if="showDeleteModal && selectedTask"
      :task="selectedTask"
      @close="closeDeleteModal"
      @deleted="handleTaskDeleted"
    />

    <!-- TOOLBAR -->
    <div class="tasks-toolbar">

      <!-- SEARCH -->
      <input
        type="text"
        placeholder="Buscar tarea..."
        class="search-input"
      />

      <!-- FILTERS -->
      <select class="filter-select">
        <option>Estado</option>
        <option>Pendiente</option>
        <option>En progreso</option>
        <option>Completada</option>
      </select>

      <select class="filter-select">
        <option>Prioridad</option>
        <option>Alta</option>
        <option>Media</option>
        <option>Baja</option>
      </select>

    </div>

    <!-- MAIN CONTENT -->
    <div class="tasks-content">

      <!-- LEFT PANEL -->
      <div class="tasks-list">

        <div
          v-for="task in tasks"
          :key="task.id"
          class="task-item"
          :class="{ active: task.id === selectedTaskId }"
          @click="selectTask(task.id)"
        >
          <div class="task-item-left">
            <input type="checkbox" :checked="task.status === 'completado' || task.status === 'completadoAtrasado'" />

            <div class="task-item-info">
              <span class="task-title">
                {{ task.title }}
              </span>

              <span class="task-date">
                📅 {{ formatDate(task.dueDate) }}
              </span>
            </div>
          </div>

          <span class="task-priority" :class="statusClass(task.status)">
            {{ statusLabel(task.status) }}
          </span>
        </div>

      </div>

      <!-- RIGHT PANEL -->
      <div class="task-details">

        <div v-if="selectedTask" class="task-details-card">

          <div class="details-header">

            <div>
              <h2>{{ selectedTask.title }}</h2>

              <p>
                {{ selectedTask.description || 'Sin descripcion' }}
              </p>
            </div>

            <span class="status" :class="statusClass(selectedTask.status)">
              {{ statusLabel(selectedTask.status) }}
            </span>

          </div>

          <div class="details-section">
            <h3>Información</h3>

            <div class="details-grid">

              <div class="detail-item">
                <span class="detail-label">Estado</span>
                <span>{{ statusLabel(selectedTask.status) }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Sector</span>
                <span>{{ selectedTask.sector?.name || 'Sin sector' }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Responsable</span>
                <span>{{ selectedTask.user?.userName || 'Sin responsable' }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Vencimiento</span>
                <span>{{ formatDate(selectedTask.dueDate) }}</span>
              </div>

            </div>
          </div>

          <div class="details-section">

            <h3>Ubicación</h3>

            <div v-if="selectedTask?.sector?.coordinates" class="map-container">
              <l-map
                :zoom="mapZoom"
                :center="mapCenter"
                style="height: 220px; width: 100%; border-radius: 12px;"
              >
                <l-tile-layer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution="&copy; OpenStreetMap contributors"
                />
                <l-marker v-if="markerPosition" :lat-lng="markerPosition" />
              </l-map>
            </div>
            <div v-else class="map-placeholder">
              🗺 El sector no tiene ubicación definida
            </div>

          </div>

          <div class="details-actions">

            <button class="secondary-btn" @click="openEditModal">
              Editar
            </button>

            <button class="danger-btn" @click="openDeleteModal">
              Eliminar
            </button>

          </div>

        </div>

        <div v-else class="task-details-card">
          <div class="details-section">
            <h3>Selecciona una tarea</h3>
            <p>Haz clic en una tarea de la izquierda para ver sus detalles.</p>
          </div>
        </div>

      </div>

    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import api from '../services/http-common.js';
import ModalNewTask from './ModalNewTask.vue';
import ModalEditTask from './ModalEditTask.vue';
import ModalDeleteTask from './ModalDeleteTask.vue';
import { LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet';
import 'leaflet/dist/leaflet.css';

const tasks = ref([]);
const selectedTaskId = ref(null);
const loading = ref(false);
const error = ref(null);
const showModal = ref(false);
const showEditModal = ref(false);
const showDeleteModal = ref(false);

const selectedTask = computed(() =>
  tasks.value.find((task) => task.id === selectedTaskId.value)
);

const markerPosition = computed(() => {
  if (!selectedTask.value?.sector?.coordinates) return null;
  const coords = selectedTask.value.sector.coordinates;
  if (coords && coords.length >= 2) {
    return [coords[1], coords[0]]; // [latitude, longitude]
  }
  return null;
});

const mapCenter = computed(() => markerPosition.value || [-33.4489, -70.6693]);
const mapZoom = computed(() => markerPosition.value ? 14 : 11);

const fetchTasks = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await api.get('/api/task');
    tasks.value = response.data || [];
    if (tasks.value.length > 0 && selectedTaskId.value == null) {
      selectedTaskId.value = tasks.value[0].id;
    }
  } catch (err) {
    console.error('Error fetching tasks:', err);
    error.value = 'No se pudieron cargar las tareas.';
  } finally {
    loading.value = false;
  }
};

const selectTask = (taskId) => {
  selectedTaskId.value = taskId;
};

const openModal = () => {
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const openEditModal = () => {
  if (!selectedTask.value) {
    return;
  }
  showEditModal.value = true;
};

const closeEditModal = () => {
  showEditModal.value = false;
};

const openDeleteModal = () => {
  if (!selectedTask.value) {
    return;
  }
  showDeleteModal.value = true;
};

const closeDeleteModal = () => {
  showDeleteModal.value = false;
};

const handleTaskCreated = () => {
  fetchTasks();
};

const handleTaskUpdated = () => {
  fetchTasks();
  showEditModal.value = false;
};

const handleTaskDeleted = () => {
  selectedTaskId.value = null;
  fetchTasks();
  showDeleteModal.value = false;
};

const formatDate = (value) => {
  if (!value) {
    return 'Sin fecha';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString('es-CL', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  });
};

const statusLabel = (status) => {
  switch (status) {
    case 'vigente':
      return 'Vigente';
    case 'atrasado':
      return 'Atrasado';
    case 'completado':
      return 'Completado';
    case 'completadoAtrasado':
      return 'Completado atrasado';
    default:
      return 'Pendiente';
  }
};

const statusClass = (status) => {
  switch (status) {
    case 'vigente':
      return 'status-vigente';
    case 'atrasado':
      return 'status-atrasado';
    case 'completado':
      return 'status-completado';
    case 'completadoAtrasado':
      return 'status-completado-atrasado';
    default:
      return 'status-pendiente';
  }
};

onMounted(fetchTasks);
</script>

<style scoped>

.tasks-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

/* HEADER */

.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tasks-header h1 {
  margin: 0;
  font-size: 32px;
}

.tasks-header p {
  margin-top: 6px;
  color: #666;
}

/* BUTTON */

.create-task-btn {
  background-color: #374151;
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.create-task-btn:hover {
  background-color: #1f2937;
}

/* TOOLBAR */

.tasks-toolbar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

.filter-select {
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background-color: white;
  font-size: 14px;
}

/* MAIN CONTENT */

.tasks-content {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  min-height: 650px;
}

/* LEFT PANEL */

.tasks-list {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);

  display: flex;
  flex-direction: column;
  gap: 12px;

  overflow-y: auto;
}

/* TASK ITEM */

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 14px;
  border-radius: 10px;

  border: 1px solid #e5e7eb;

  cursor: pointer;
  transition: 0.2s;
}

.task-item:hover {
  background-color: #f9fafb;
}

.task-item.active {
  border: 2px solid #3b82f6;
  background-color: #eff6ff;
}

.task-item-left {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.task-item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.task-title {
  font-weight: 600;
}

.task-date {
  color: #666;
  font-size: 14px;
}

/* PRIORITY */

.task-priority {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: bold;
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

/* RIGHT PANEL */

.task-details {
  display: flex;
}

.task-details-card {
  width: 100%;
  background-color: white;
  border-radius: 12px;
  padding: 24px;

  box-shadow: 0 2px 8px rgba(0,0,0,0.08);

  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* DETAILS HEADER */

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.details-header h2 {
  margin: 0;
}

.details-header p {
  color: #666;
  margin-top: 8px;
  line-height: 1.5;
}

/* STATUS */

.status {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: bold;
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

/* DETAILS */

.details-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.details-section h3 {
  margin: 0;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  color: #666;
  font-size: 14px;
}

/* MAP */

.map-placeholder {
  height: 220px;
  border-radius: 12px;
  border: 2px dashed #cbd5e1;

  display: flex;
  justify-content: center;
  align-items: center;

  color: #64748b;
  background-color: #f8fafc;
}

.map-container {
  height: 220px;
  border-radius: 12px;
  overflow: hidden;
}

/* ACTIONS */

.details-actions {
  display: flex;
  gap: 12px;
}

.secondary-btn,
.danger-btn {
  padding: 12px 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.secondary-btn {
  background-color: #374151;
  color: white;
}

.danger-btn {
  background-color: #ef4444;
  color: white;
}

/* RESPONSIVE */

@media (max-width: 1000px) {

  .tasks-content {
    grid-template-columns: 1fr;
  }

}

</style>