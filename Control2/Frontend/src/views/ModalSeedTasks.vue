<template>
  <div class="modal-backdrop" @click.self="emitClose">
    <div class="modal-card">
      <div class="modal-header">
        <h2>Generar Datos de Prueba</h2>
        <button class="icon-btn" @click="emitClose">x</button>
      </div>

      <div class="modal-body">
        <div class="field">
          <label>Tareas por usuario:</label>
          <input v-model.number="seedData.countPerUser" type="number" class="form-input" />
        </div>

        <div class="field">
          <label>IDs de Usuarios (separados por coma):</label>
          <input v-model="userString" type="text" placeholder="1, 2, 3" class="form-input" />
        </div>

        <div class="field">
          <label>IDs de Sectores (separados por coma):</label>
          <input v-model="sectorString" type="text" placeholder="10, 11" class="form-input" />
        </div>

        <div class="field">
          <label>Distribución de estados (%):</label>
          <div class="dist-grid">
            <input v-model.number="seedData.statusDistribution.vigente" type="number" placeholder="Vigente" title="Vigente" />
            <input v-model.number="seedData.statusDistribution.atrasado" type="number" placeholder="Atrasado" title="Atrasado" />
            <input v-model.number="seedData.statusDistribution.completado" type="number" placeholder="Completado" title="Completado" />
            <input v-model.number="seedData.statusDistribution.completadoAtrasado" type="number" placeholder="Comp. Atrasado" title="Comp. Atrasado" />
          </div>
        </div>

        <div class="modal-actions">
          <button class="secondary-btn" @click="emitClose">Cancelar</button>
          <button class="primary-btn" :disabled="loading" @click="confirmSeed">
            {{ loading ? 'Generando...' : 'Generar Tareas' }}
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

const emit = defineEmits(['close', 'seeded']);
const loading = ref(false);
const error = ref(null);
const { show } = useAlert();

const seedData = ref({
  countPerUser: null,
  targetUserIds: [],
  targetSectorIds: [],
  statusDistribution: { 
    vigente: null, 
    atrasado: null, 
    completado: null, 
    completadoAtrasado: null 
  }
});

const userString = ref(""); 
const sectorString = ref("");

const confirmSeed = async () => {
  error.value = null;

  // Validación básica en Frontend
  if (!seedData.value.countPerUser || !userString.value || !sectorString.value) {
    error.value = "Por favor, completa todos los campos obligatorios.";
    show({ message: error.value, severity: 'warning', autoHideMs: 3000 });
    return;
  }

  loading.value = true;

  try {
    // Preparación del payload
    const payload = {
      ...seedData.value,
      targetUserIds: userString.value.split(',').map(n => parseInt(n.trim())).filter(n => !isNaN(n)),
      targetSectorIds: sectorString.value.split(',').map(n => parseInt(n.trim())).filter(n => !isNaN(n))
    };

    // Petición al backend
    await api.post('/api/task/seed', payload);

    show({ message: 'Datos generados correctamente', severity: 'success', autoHideMs: 3000 });
    emit('seeded'); 
  } catch (err) {
    console.error("Error al generar datos:", err);
    
    // Si el backend envió un mensaje claro (ej: "No existen sectores creados"), lo mostramos
    const errorMessage = (err.response && err.response.data) 
                         ? err.response.data 
                         : 'Ocurrió un error inesperado al generar los datos.';
    
    error.value = errorMessage;
    show({ message: errorMessage, severity: 'error', autoHideMs: 5000 });
  } finally {
    loading.value = false;
  }
};

const emitClose = () => emit('close');
</script>

<style scoped>
.dist-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.dist-grid input {
  padding: 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

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
  width: min(88vw, 520px);
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

.modal-body {
  padding: 20px 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-input {
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
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

.primary-btn {
  background-color: #000000;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}
</style>