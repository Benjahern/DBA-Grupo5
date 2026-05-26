<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card modal-card--compact">
      <div class="modal-header">
        <h2>Eliminar tarea</h2>
        <button class="icon-btn" type="button" @click="emitClose">x</button>
      </div>

      <div class="modal-body">
        <p class="confirm-text">
          ¿Estás seguro de eliminar la tarea
          <span class="task-name">{{ task.title }}</span>?
          Esta acción no se puede deshacer.
        </p>

        <p v-if="error" class="form-error">{{ error }}</p>

        <div class="modal-actions">
          <button class="secondary-btn" type="button" @click="emitClose">
            Cancelar
          </button>
          <button class="danger-btn" type="button" :disabled="loading" @click="handleDelete">
            {{ loading ? 'Eliminando...' : 'Eliminar' }}
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
  task: {
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
  error.value = null;
  loading.value = true;
  try {
    await api.delete(`/api/task/${props.task.id}`);
    show({ message: 'Tarea eliminada correctamente.', severity: 'success', autoHideMs: 3000 });
    emit('deleted');
    emitClose();
  } catch (err) {
    console.error('Error deleting task:', err);
    error.value = 'No se pudo eliminar la tarea.';
    show({ message: 'No se pudo eliminar la tarea.', severity: 'error', autoHideMs: 4000 });
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
  padding: 32px 56px 32px 32px;
  z-index: 1200;
}

.modal-card {
  width: min(88vw, 520px);
  background: white;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  overflow: hidden;
  box-sizing: border-box;
}

.modal-card--compact {
  width: min(88vw, 520px);
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

.confirm-text {
  margin: 0;
  color: #1f2937;
  line-height: 1.5;
}

.task-name {
  font-weight: 600;
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

.secondary-btn {
  background-color: #e2e8f0;
  color: #1f2937;
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
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

@media (max-width: 600px) {
  .modal-backdrop {
    padding: 24px 28px;
  }
}
</style>
