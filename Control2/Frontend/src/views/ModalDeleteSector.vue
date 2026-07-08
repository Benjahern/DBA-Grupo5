<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card">
      <div class="modal-header">
        <h2>Eliminar Sector</h2>
        <button class="icon-btn" type="button" @click="emitClose">x</button>
      </div>

      <div class="modal-body">
        <p>¿Estás seguro de que deseas eliminar el sector <strong>{{ sector?.name }}</strong>?</p>
        <p class="warning-text">Esta acción no se puede deshacer.</p>

        <p v-if="error" class="form-error">{{ error }}</p>

        <div class="modal-actions">
          <button class="secondary-btn" type="button" @click="emitClose">
            Cancelar
          </button>
          <button class="danger-btn" type="button" @click="handleDelete" :disabled="loading">
            {{ loading ? 'Eliminando...' : 'Sí, eliminar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import api from '../services/http-common.js';
import { useAlert } from '../components/Alerts/useAlert.js';

const props = defineProps({
  sector: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'deleted']);

const loading = ref(false);
const error = ref(null);
const { show } = useAlert();

const emitClose = () => {
  emit('close');
};

const handleDelete = async () => {
  loading.value = true;
  error.value = null;

  try {
    await api.delete(`/api/sectors/${props.sector.id}`);
    show({ message: 'Sector eliminado correctamente.', severity: 'success', autoHideMs: 3000 });
    emit('deleted');
    emitClose();
  } catch (err) {
    console.error('Error deleting sector:', err);
    if (err.response && err.response.data) {
        error.value = typeof err.response.data === 'string' ? err.response.data : 'Error al eliminar el sector.';
    } else {
        error.value = 'No se pudo eliminar el sector. Puede que tenga tareas asociadas.';
    }
    show({ message: error.value, severity: 'error', autoHideMs: 5000 });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  z-index: 1200;
}

.modal-card {
  width: min(88vw, 450px);
  background: white;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  overflow: hidden;
  box-sizing: border-box;
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
  color: #b91c1c;
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

.warning-text {
  color: #b91c1c;
  font-size: 0.9rem;
  margin-top: -10px;
}

.form-error {
  color: #b91c1c;
  font-size: 0.9rem;
  background: #fef2f2;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.danger-btn {
  background-color: #dc2626;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.danger-btn:disabled {
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
</style>
