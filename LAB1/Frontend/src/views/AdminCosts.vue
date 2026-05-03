<template>
  <section class="dashboard">
    <div class="instances-card">
      <header class="card-header">
        <div class="title-section">
          <h2>Todos los costos (Admin)</h2>
          <h3>{{ activeCount }} instancias activas de {{ totalCount }} usuarios</h3>
          <p>Resumen de costos de todas las instancias por usuario.</p>
        </div>
        <button @click="initDashboard" class="action-btn reload-btn">
          🔄 Recargar
        </button>
      </header>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando instancias y recursos...</p>
      </div>

      <div v-else-if="viewInstances.length === 0" class="empty-state">
        <p>No hay instancias creadas actualmente.</p>
      </div>

      <div v-else class="instance-list">
        <div
          v-for="(instance, index) in viewInstances"
          :key="instance.id ?? index"
          class="instance-card"
          :class="{ 'is-expanded': expandedId === instance.id }"
        >
          <div class="summary-row">
            <div class="info-column">
              <h3>{{ instance.name }}</h3>
              <p class="region">📍 {{ instance.regionName }}</p>
              <p class="owner">👤 Usuario ID: {{ instance.userId }}</p>
            </div>
            <div class="specs-column">
              <div class="spec-item">
                <span class="spec-label">CPU</span>
                <div class="spec-data">
                  <span class="icon">💻</span>
                  <div class="spec-text">
                    <span class="val">{{ instance.cpu.quantity }}</span>
                    <span class="lbl">vCPUs</span>
                  </div>
                </div>
              </div>

              <div class="spec-item">
                <span class="spec-label">RAM</span>
                <div class="spec-data">
                  <span class="icon">🧠</span>
                  <div class="spec-text">
                    <span class="val">{{ instance.ram.quantity }}</span>
                    <span class="lbl">GB</span>
                  </div>
                </div>
              </div>

              <div class="spec-item">
                <span class="spec-label">Storage</span>
                <div class="spec-data">
                  <span class="icon">💾</span>
                  <div class="spec-text">
                    <span class="val">{{ instance.storage.quantity }}</span>
                    <span class="lbl">GB</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="action-column">
              <span class="status-badge" :class="instance.state.toLowerCase()">
                {{ instance.state }}
              </span>
              <button @click="toggleDetail(instance.id)" class="action-btn detail-btn">
                {{ expandedId === instance.id ? 'Ocultar detalle ▲' : 'Ver detalle ▼' }}
              </button>
            </div>
          </div>

          <transition name="expand">
            <div v-if="expandedId === instance.id" class="detail-section">
              <div class="detail-divider"></div>

              <div class="ticket-container">
                <div class="ticket-panel">
                  <div class="ticket-header">
                    <h3 class="panel-title">🧾 Ticket de Consumo Acumulado</h3>
                    <div class="uptime-badge">
                      <span class="uptime-label">Tiempo Activo:</span>
                      <span class="uptime-value">{{ instance.hoursActive.toFixed(1) }} Horas</span>
                    </div>
                  </div>

                  <table class="billing-table">
                    <thead>
                      <tr>
                        <th>Recurso</th>
                        <th class="text-center">Precio x Hora</th>
                        <th class="text-center">Horas de Uso</th>
                        <th class="text-right">Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>Procesamiento (CPU)</td>
                        <td class="text-center">${{ instance.cpu.cost_ph.toFixed(3) }}</td>
                        <td class="text-center">{{ instance.hoursActive.toFixed(1) }}</td>
                        <td class="text-right">${{ (instance.cpu.cost_ph * instance.hoursActive).toFixed(2) }}</td>
                      </tr>
                      <tr>
                        <td>Memoria (RAM)</td>
                        <td class="text-center">${{ instance.ram.cost_ph.toFixed(3) }}</td>
                        <td class="text-center">{{ instance.hoursActive.toFixed(1) }}</td>
                        <td class="text-right">${{ (instance.ram.cost_ph * instance.hoursActive).toFixed(2) }}</td>
                      </tr>
                      <tr>
                        <td>Almacenamiento (SSD)</td>
                        <td class="text-center">${{ instance.storage.cost_ph.toFixed(3) }}</td>
                        <td class="text-center">{{ instance.hoursActive.toFixed(1) }}</td>
                        <td class="text-right">${{ (instance.storage.cost_ph * instance.hoursActive).toFixed(2) }}</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <td colspan="3" class="total-label">Costo Total Estimado</td>
                        <td class="total-amount">${{ calculateTotal(instance).toFixed(2) }}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import api from '../services/http-common.js';
import { useAlert } from '../components/Alerts/useAlert.js';

const { show } = useAlert();

const instances = ref([]);
const isLoading = ref(false);

const cpusCatalog = ref([]);
const ramsCatalog = ref([]);
const storagesCatalog = ref([]);

const expandedId = ref(null);

// REGIONES PROVISIONALES
const localRegions = [
  { id: 1, name: 'Texas' },
  { id: 2, name: 'SA East (São Paulo)' },
  { id: 3, name: 'EU West (Ireland)' }
];

const loadCatalogs = async () => {
  try {
    const [cpuRes, ramRes, stRes] = await Promise.all([
      api.get('/api/cpus').catch(() => ({ data: [] })),
      api.get('/api/rams').catch(() => ({ data: [] })),
      api.get('/api/storages').catch(() => ({ data: [] }))
    ]);
    cpusCatalog.value = cpuRes.data;
    ramsCatalog.value = ramRes.data;
    storagesCatalog.value = stRes.data;
  } catch (e) {
    console.error('No se pudieron cargar los catálogos', e);
  }
};

const parseHardware = (id, catalog, fieldKey1, fieldKey2) => {
  if (!id) return { quantity: 0, cost_ph: 0 };
  const found = catalog.find(x => x[fieldKey1] == id || x[fieldKey2] == id || x.id == id);
  if (found) {
    return {
      quantity: found.quantity ?? found.Quantity ?? 0,
      cost_ph: found.cost_ph ?? found.Cost_ph ?? 0
    };
  }
  return { quantity: 0, cost_ph: 0 };
};

const parseDurationToHours = (durationStr) => {
  if (!durationStr) return 0;
  if (typeof durationStr === 'number') return durationStr;

  if (typeof durationStr === 'string' && durationStr.startsWith('PT')) {
    const hoursMatch = durationStr.match(/(\d+(\.\d+)?)H/);
    const minsMatch = durationStr.match(/(\d+(\.\d+)?)M/);
    let h = hoursMatch ? parseFloat(hoursMatch[1]) : 0;
    let m = minsMatch ? parseFloat(minsMatch[1]) : 0;
    return h + (m / 60);
  }

  const parsed = parseFloat(durationStr);
  return isNaN(parsed) ? 0 : parsed;
};

const toViewInstance = (raw) => {
  const rawHours = raw?.active_hours ?? raw?.Active_hours;
  let computedHours = parseDurationToHours(rawHours);

  const currentState = String(raw?.state ?? raw?.State ?? '').toUpperCase();
  if (computedHours === 0 && (currentState === 'RUNNING' || currentState === 'ACTIVO')) {
    computedHours = 1;
  }

  const regId = raw?.region_id ?? raw?.Region_id;
  const foundRegion = localRegions.find(r => r.id == regId);
  const regionNameStr = foundRegion ? foundRegion.name : `Region #${regId || 'Desconocida'}`;

  return {
    id: raw?.instance_id ?? raw?.Instance_id ?? raw?.id,
    name: raw?.name ?? raw?.Name ?? 'Servidor sin nombre',
    regionName: regionNameStr,
    ip: raw?.ip_address ?? raw?.Ip_address ?? 'Pendiente',
    state: raw?.state ?? raw?.State ?? 'Unknown',
    hoursActive: computedHours,
    userId: raw?.user_id ?? raw?.User_id ?? '-',
    cpu: parseHardware(raw?.cpu_id ?? raw?.Cpu_id, cpusCatalog.value, 'cpu_id', 'Cpu_id'),
    ram: parseHardware(raw?.ram_id ?? raw?.Ram_id, ramsCatalog.value, 'ram_id', 'Ram_id'),
    storage: parseHardware(raw?.storage_id ?? raw?.Storage_id, storagesCatalog.value, 'storage_id', 'Storage_id'),
  };
};

const viewInstances = computed(() => instances.value.map(toViewInstance));

const activeCount = computed(() =>
  viewInstances.value.filter((i) =>
    String(i.state || '').toUpperCase() === 'RUNNING' || String(i.state || '').toUpperCase() === 'ACTIVO'
  ).length
);

const totalCount = computed(() => viewInstances.value.length);

const fetchInstances = async () => {
  isLoading.value = true;
  try {
    const response = await api.get('/api/instances');
    instances.value = Array.isArray(response.data) ? response.data : [];

    if (instances.value.length === 0) {
      show({
        message: 'No se cuenta con ninguna instancia creada.',
        severity: 'warning',
        autoHideMs: 4000
      });
    }
  } catch (err) {
    show({
      message: 'No se pudieron cargar las instancias. Intenta nuevamente.',
      severity: 'error',
      autoHideMs: 4000
    });
  } finally {
    isLoading.value = false;
  }
};

const initDashboard = async () => {
  await loadCatalogs();
  await fetchInstances();
};

onMounted(initDashboard);

const toggleDetail = (id) => {
  expandedId.value = expandedId.value === id ? null : id;
};

const calculateTotal = (instance) => {
  const totalCostPerHour = instance.cpu.cost_ph + instance.ram.cost_ph + instance.storage.cost_ph;
  return totalCostPerHour * instance.hoursActive;
};
</script>

<style scoped>
.dashboard {
  display: flex;
  justify-content: center;
  padding: 24px 16px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.instances-card {
  width: 100%;
  max-width: 1100px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title-section h2 { margin: 0 0 4px 0; font-size: 22px; color: #1e293b; }
.title-section h3 { margin: 0 0 4px 0; font-size: 16px; color: #64748b; }
.title-section p { margin: 0; color: #64748b; font-size: 14px; }

.loading-state, .empty-state {
  text-align: center;
  padding: 48px;
  color: #64748b;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.spinner {
  border: 3px solid #f1f5f9;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px auto;
}

@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.instance-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  margin-bottom: 16px;
  transition: all 0.2s ease;
  overflow: hidden;
}

.instance-card.is-expanded {
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border-color: #bfdbfe;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background-color: #fff;
  position: relative;
  z-index: 2;
}

.info-column { flex: 1.5; }
.info-column h3 { margin: 0 0 6px 0; font-size: 18px; color: #1e293b; font-weight: 700; }
.info-column .region { margin: 0 0 4px 0; font-size: 13px; color: #94a3b8; }
.info-column .owner { margin: 0; font-size: 13px; color: #94a3b8; }

.specs-column {
  flex: 2;
  display: flex;
  gap: 32px;
  justify-content: center;
}

.spec-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.spec-label {
  font-size: 11px;
  text-transform: uppercase;
  color: #94a3b8;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.spec-data {
  display: flex;
  align-items: center;
  gap: 10px;
}

.spec-item .icon {
  font-size: 20px;
}

.spec-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.spec-text .val {
  font-size: 15px;
  font-weight: 700;
  color: #334155;
}

.spec-text .lbl {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.action-column {
  flex: 1.5;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.reload-btn { background-color: #f1f5f9; color: #475569; }
.reload-btn:hover { background-color: #e2e8f0; }

.detail-btn {
  background-color: #f8fafc;
  color: #1e293b;
  border: 1px solid #cbd5e1;
}
.detail-btn:hover { background-color: #f1f5f9; }

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.status-badge.running, .status-badge.activo { background-color: #dcfce7; color: #16a34a; border: 1px solid #bbf7d0; }
.status-badge.stopped, .status-badge.detenido { background-color: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
.status-badge.unknown { background-color: #f1f5f9; color: #64748b; }

.detail-section {
  padding: 0 24px 24px 24px;
  background-color: #ffffff;
}

.detail-divider {
  height: 1px;
  background-color: #f1f5f9;
  margin-bottom: 24px;
}

.ticket-container {
  display: flex;
  justify-content: center;
}

.ticket-panel {
  width: 100%;
  max-width: 900px;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-title {
  margin: 0;
  font-size: 18px;
  color: #2563eb;
  font-weight: 700;
}

.uptime-badge {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #334155;
}
.uptime-value {
  font-weight: 800;
  color: #1e293b;
  margin-left: 6px;
}

.billing-table {
  width: 100%;
  border-collapse: collapse;
}

.billing-table th, .billing-table td {
  padding: 16px 10px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  color: #475569;
}
.billing-table th {
  color: #64748b;
  font-weight: 600;
  text-align: left;
  border-bottom: 2px solid #e2e8f0;
}
.billing-table .text-right { text-align: right; }
.billing-table .text-center { text-align: center; }

.billing-table tfoot .total-label {
  font-weight: 800;
  text-align: right;
  padding-top: 24px;
  color: #1e293b;
  font-size: 16px;
}
.billing-table tfoot .total-amount {
  font-weight: 800;
  text-align: right;
  color: #10b981;
  font-size: 20px;
  padding-top: 24px;
}

.expand-enter-active, .expand-leave-active { transition: all 0.3s ease-in-out; max-height: 600px; opacity: 1; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; padding: 0; margin: 0; }

@media (max-width: 900px) {
  .summary-row { flex-direction: column; align-items: flex-start; gap: 16px; }
  .specs-column { justify-content: flex-start; width: 100%; flex-wrap: wrap; }
  .action-column { width: 100%; justify-content: space-between; }
  .ticket-header { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>