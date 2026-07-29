<template>
  <div class="confirm-overlay" @click.self="emit('cancel')">
    <div class="confirm-card">
      <header class="modal-header">
        <h2>¿Estas seguro?</h2>
        <button class="close-btn" @click="emit('cancel')">X</button>
      </header>
      <div class="confirm-body">
        <div class="summary">
          <span class="resume">Resumen</span>
          <dl>
            <div>
              <dt>Nombre</dt>
              <dd>{{ summary.name || 'Sin nombre' }}</dd>
            </div>
            <div>
              <dt>Region</dt>
              <dd>{{ summary.region || 'Sin seleccionar' }}</dd>
            </div>
            <div>
              <dt>Datacenter</dt>
              <dd>{{ summary.datacenter || 'Sin seleccionar' }}</dd>
            </div>
            <div>
              <dt>CPU</dt>
              <dd>{{ summary.cpu || 'Sin seleccionar' }}</dd>
            </div>
            <div>
              <dt>RAM</dt>
              <dd>{{ summary.ram || 'Sin seleccionar' }}</dd>
            </div>
            <div>
              <dt>Almacenamiento</dt>
              <dd>{{ summary.storage || 'Sin seleccionar' }}</dd>
            </div>
          </dl>
        </div>
        <div class="form-actions">
          <button type="button" class="btn primary" @click="emit('confirm')">Confirmar</button>
          <button type="button" class="btn ghost" @click="emit('cancel')">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits(['confirm', 'cancel']);
const props = defineProps({
  summary: {
    type: Object,
    default: () => ({
      name: '',
      region: '',
      datacenter: '',
      cpu: '',
      ram: '',
      storage: ''
    })
  }
});

const summary = props.summary;
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.confirm-card {
  width: min(500px, 100vw);
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.resume{
    display: block;
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
    margin-bottom: 8px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
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

.confirm-body {
  padding-top: 8px;
}

.summary {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 14px;
}

.summary h3 {
  margin: 0 0 8px;
  font-size: 15px;
}

.summary dl {
  display: grid;
  gap: 10px;
  margin: 0;
}

.summary dl div {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 16px;
  align-items: center;
  padding: 4px 0;
}

.summary dt {
  font-weight: 600;
  color: #0f172a;
  text-align: left;
}

.summary dd {
  margin: 0;
  color: #475569;
  text-align: left;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 600;
  cursor: pointer;
}

.btn.ghost {
  background: #e2e8f0;
  color: #0f172a;
}

.btn.primary {
  background: #2563eb;
  color: #ffffff;
}
</style>
