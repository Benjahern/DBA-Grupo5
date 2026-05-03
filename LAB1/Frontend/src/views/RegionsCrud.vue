<template>
  <div class="regions-page">
    <header class="hero">
      <h1>Gestión de Regiones</h1>
      <p>Administra las regiones de los datacenters</p>
    </header>

    <main class="crud-container">
      <div class="card">
        <header class="card-header">
          <h2>Regiones Registradas</h2>
          <button class="action-btn" @click="openCreateModal">
            Nueva Región
          </button>
        </header>

        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">Nombre</div>
          <div class="table-cell actions-cell">Acciones</div>
        </div>

        <div v-if="isLoading" class="loading">Cargando regiones...</div>
        
        <div v-else class="region-list">
          <div v-for="region in regions" :key="region.id" class="region-row">
            <p>{{ region.id }}</p>
            <p>{{ region.name }}</p>
            <div class="actions">
              <button class="btn edit-btn" @click="openEditModal(region)">Editar</button>
              <button class="btn delete-btn" @click="deleteRegion(region.id)">Eliminar</button>
            </div>
          </div>
          <div v-if="regions.length === 0" class="empty-state">
            No hay regiones registradas.
          </div>
        </div>
      </div>
    </main>

    <!-- Modal de Creación / Edición -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <header class="modal-header">
          <h2>{{ isEditing ? 'Editar Región' : 'Nueva Región' }}</h2>
          <button class="close-btn" @click="closeModal">X</button>
        </header>
        <form @submit.prevent="saveRegion" class="modal-body">
          <div class="input-group">
            <label for="region-name">Nombre de la Región</label>
            <input 
              id="region-name" 
              v-model="currentRegion.name" 
              placeholder="Ej: us-east-1" 
              required 
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn primary" :disabled="isSaving">
              {{ isSaving ? 'Guardando...' : 'Guardar' }}
            </button>
            <button type="button" class="btn ghost" @click="closeModal">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../services/http-common.js';
import { useAlert } from '../components/Alerts/useAlert.js';

const { show } = useAlert();

// Estado
const regions = ref([]);
const isLoading = ref(false);
const showModal = ref(false);
const isEditing = ref(false);
const isSaving = ref(false);
const currentRegion = ref({ id: null, name: '' });

// Cargar Regiones
const fetchRegions = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/api/regions');
    regions.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    show({ message: 'Error al cargar las regiones', severity: 'error', autoHideMs: 4000 });
  } finally {
    isLoading.value = false;
  }
};

// Modal Actions
const openCreateModal = () => {
  isEditing.value = false;
  currentRegion.value = { id: null, name: '' };
  showModal.value = true;
};

const openEditModal = (region) => {
  isEditing.value = true;
  currentRegion.value = { ...region };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

// Guardar (Crear o Actualizar)
const saveRegion = async () => {
  isSaving.value = true;
  try {
    if (isEditing.value) {
      await api.put(`/api/regions/${currentRegion.value.id}`, { region_id: currentRegion.value.id, Name: currentRegion.value.name });
      show({ message: 'Región actualizada exitosamente', severity: 'success', autoHideMs: 4000 });
    } else {
      await api.post('/api/regions', { Name: currentRegion.value.name });
      show({ message: 'Región creada exitosamente', severity: 'success', autoHideMs: 4000 });
    }
    closeModal();
    fetchRegions();
  } catch (error) {
    const msg = error.response?.status === 401 
      ? 'No tienes permisos de Sysadmin para realizar esta acción.' 
      : 'Error al guardar la región';
    show({ message: msg, severity: 'error', autoHideMs: 4000 });
  } finally {
    isSaving.value = false;
  }
};

// Eliminar
const deleteRegion = async (id) => {
  if (!confirm('¿Estás seguro de que deseas eliminar esta región? Esta acción no se puede deshacer.')) return;
  
  try {
    await api.delete(`/api/regions/${id}`);
    show({ message: 'Región eliminada exitosamente', severity: 'success', autoHideMs: 4000 });
    fetchRegions();
  } catch (error) {
    const msg = error.response?.status === 401 
      ? 'No tienes permisos de Sysadmin para eliminar regiones.' 
      : 'Error al eliminar la región (puede estar en uso).';
    show({ message: msg, severity: 'error', autoHideMs: 4000 });
  }
};

onMounted(() => {
  fetchRegions();
});
</script>

<style scoped>
.regions-page {
  max-width: 900px;
  width: 90%;
  margin: 0 auto; 
  padding-top: 2rem;
}

.hero {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  text-align: center;
}

.card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1fr 150px;
  font-weight: bold;
  padding: 10px;
  border-bottom: 2px solid #eaeaea;
  margin-bottom: 10px;
}

.region-row {
  display: grid;
  grid-template-columns: 80px 1fr 150px;
  align-items: center;
  padding: 12px 10px;
  border-bottom: 1px solid #eaeaea;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}

.edit-btn { background-color: #f59e0b; color: white; }
.edit-btn:hover { background-color: #d97706; }

.delete-btn { background-color: #ef4444; color: white; }
.delete-btn:hover { background-color: #dc2626; }

.action-btn {
  background-color: #42b883;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}
.action-btn:hover { background-color: #33a06f; }

/* Modal Styles similares a ConfirmInstance.vue */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-card {
  width: min(400px, 100vw);
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.close-btn {
  border: none;
  background: #e2e8f0;
  color: #0f172a;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
}

.input-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.input-group label {
  margin-bottom: 6px;
  font-weight: 600;
  color: #333;
}

.input-group input {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.ghost { background: transparent; color: #64748b; }
.ghost:hover { background: #f1f5f9; }
</style>