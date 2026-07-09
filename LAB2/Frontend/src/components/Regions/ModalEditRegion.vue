<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card">
      <div class="modal-header">
        <h2>Editar Región</h2>
        <button class="icon-btn" type="button" @click="emitClose">x</button>
      </div>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="region-name">Nombre de la Región</label>
          <input
            id="region-name"
            v-model.trim="form.Name"
            type="text"
            placeholder="Nombre de la región"
            required
          />
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
import { ref, watch } from 'vue';
import api from '../../services/http-common.js';
import { useAlert } from '../Alerts/useAlert.js';

const props = defineProps({
  region: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'updated']);

const form = ref({
  Name: ''
});

const loading = ref(false);
const error = ref(null);
const { show } = useAlert();

watch(
  () => props.region,
  (value) => {
    if (value) {
      form.value.Name = value.Name || '';
    }
  },
  { immediate: true }
);

const emitClose = () => {
  emit('close');
};

const handleSubmit = async () => {
  error.value = null;
  if (!form.value.Name) {
    error.value = 'El nombre no puede estar vacío.';
    show({ message: 'El nombre es obligatorio.', severity: 'warning', autoHideMs: 4000 });
    return;
  }

  loading.value = true;
  try {
    await api.put(`/api/regions/${props.region.region_id}`, {
      Name: form.value.Name
    });
    show({ message: 'Región actualizada correctamente.', severity: 'success', autoHideMs: 3000 });
    emit('updated');
    emitClose();
  } catch (err) {
    console.error('Error updating region:', err);
    error.value = 'No se pudo actualizar la región.';
    show({ message: 'Error al actualizar la región.', severity: 'error', autoHideMs: 4000 });
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
  width: min(88vw, 500px);
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

input {
  width: 100%;
  padding: 11px 12px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  font-size: 0.95rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

input:focus {
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
</style>