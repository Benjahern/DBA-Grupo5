<template>
  <section class="bandwidth-dashboard">
    <div class="bandwidth-card">
      <header class="card-header">
        <div class="title-section">
          <h2>📊 Reporte de Ancho de Banda</h2>
          <p>Consumo de ancho de banda y costo asociado por cliente y periodo de facturación.</p>
        </div>
        <div class="header-actions">
          <div class="period-selector">
            <label for="period-select">Periodo:</label>
            <input
              id="period-select"
              type="month"
              v-model="selectedPeriod"
              @change="loadData"
              class="period-input"
            />
          </div>
          <button @click="loadData" class="action-btn reload-btn">🔄 Recargar</button>
        </div>
      </header>

      <!-- Loading -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando reportes de ancho de banda...</p>
      </div>

      <div v-else>
        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- Pipeline 1: Consumo por cliente ($group)                     -->
        <!-- ══════════════════════════════════════════════════════════════ -->
        <div class="section-block">
          <div class="section-header">
            <h3>📈 Consumo y Costo por Cliente</h3>
          </div>
          <p class="section-desc">
            Desglose detallado del tráfico de red generado por cada cliente y su costo asociado según el tarifario vigente.
          </p>

          <div v-if="costReport.length === 0" class="empty-state">
            <p>No hay datos de consumo para el periodo {{ selectedPeriod }}.</p>
          </div>

          <div v-else class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Usuario ID</th>
                  <th class="text-right">Bytes In</th>
                  <th class="text-right">Bytes Out</th>
                  <th class="text-right">Total (GB)</th>
                  <th class="text-center">Instancias</th>
                  <th class="text-center">Registros</th>
                  <th class="text-right cost-col">Costo Ancho de Banda</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in costReport" :key="row.userId">
                  <td>
                    <span class="user-badge">👤 {{ row.userId }}</span>
                  </td>
                  <td class="text-right mono">{{ formatBytes(row.totalBytesIn) }}</td>
                  <td class="text-right mono">{{ formatBytes(row.totalBytesOut) }}</td>
                  <td class="text-right mono highlight">{{ row.totalGb?.toFixed(2) }} GB</td>
                  <td class="text-center">{{ row.instanceCount }}</td>
                  <td class="text-center">{{ row.recordCount }}</td>
                  <td class="text-right cost-col">
                    <span class="cost-value">${{ row.bandwidthCost?.toFixed(2) }}</span>
                  </td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td class="total-label">Total</td>
                  <td class="text-right mono">{{ formatBytes(totalBytesIn) }}</td>
                  <td class="text-right mono">{{ formatBytes(totalBytesOut) }}</td>
                  <td class="text-right mono highlight">{{ totalGb.toFixed(2) }} GB</td>
                  <td class="text-center">{{ totalInstances }}</td>
                  <td class="text-center">{{ totalRecords }}</td>
                  <td class="text-right">
                    <span class="total-cost">${{ totalCost.toFixed(2) }}</span>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        <!-- ══════════════════════════════════════════════════════════════ -->
        <!-- Pipeline 2: Distribución por rangos ($bucket)                -->
        <!-- ══════════════════════════════════════════════════════════════ -->
        <div class="section-block">
          <div class="section-header">
            <h3>📊 Distribución de Clientes por Rangos</h3>
          </div>
          <p class="section-desc">
            Clasificación de los clientes según su volumen de consumo de red, permitiendo identificar los perfiles de uso.
          </p>

          <div v-if="bucketReport.length === 0" class="empty-state">
            <p>No hay datos de distribución para el periodo {{ selectedPeriod }}.</p>
          </div>

          <div v-else>
            <!-- Visual bar chart -->
            <div class="bucket-chart">
              <div
                v-for="(bucket, idx) in bucketReport"
                :key="idx"
                class="bucket-bar-container"
              >
                <div class="bucket-label">{{ bucket.bucketLabel }}</div>
                <div class="bucket-bar-wrapper">
                  <div
                    class="bucket-bar"
                    :style="{ width: bucketBarWidth(bucket.clientCount) + '%' }"
                    :class="'bucket-color-' + idx"
                  >
                    <span class="bar-text">{{ bucket.clientCount }} cliente(s)</span>
                  </div>
                </div>
                <div class="bucket-stats">
                  <span class="bucket-gb">{{ bucket.totalGbInBucket?.toFixed(2) }} GB</span>
                  <span class="bucket-cost">${{ bucket.totalCostInBucket?.toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- Table -->
            <div class="table-container">
              <table class="data-table bucket-table">
                <thead>
                  <tr>
                    <th>Rango de Consumo</th>
                    <th class="text-center">Clientes</th>
                    <th class="text-right">Total GB</th>
                    <th class="text-right">Costo Total</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(bucket, idx) in bucketReport" :key="idx">
                    <td>
                      <span class="range-badge" :class="'bucket-bg-' + idx">
                        {{ bucket.bucketLabel }}
                      </span>
                    </td>
                    <td class="text-center">{{ bucket.clientCount }}</td>
                    <td class="text-right mono">{{ bucket.totalGbInBucket?.toFixed(2) }} GB</td>
                    <td class="text-right">
                      <span class="cost-value">${{ bucket.totalCostInBucket?.toFixed(2) }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Pricing reference -->
        <div class="section-block pricing-info">
          <h4>💲 Esquema de Pricing de Ancho de Banda</h4>
          <div class="pricing-grid">
            <div class="pricing-tier free">
              <span class="tier-range">0 – 10 GB</span>
              <span class="tier-price">Gratis</span>
            </div>
            <div class="pricing-tier">
              <span class="tier-range">10 – 100 GB</span>
              <span class="tier-price">$0.05 / GB</span>
            </div>
            <div class="pricing-tier">
              <span class="tier-range">100 – 1,000 GB</span>
              <span class="tier-price">$0.03 / GB</span>
            </div>
            <div class="pricing-tier">
              <span class="tier-range">1,000+ GB</span>
              <span class="tier-price">$0.01 / GB</span>
            </div>
          </div>
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

const isLoading = ref(false);
const costReport = ref([]);
const bucketReport = ref([]);

// Default: mes actual en formato YYYY-MM
const now = new Date();
const selectedPeriod = ref(
  `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
);

// Totales computados para Pipeline 1
const totalBytesIn = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.totalBytesIn || 0), 0)
);
const totalBytesOut = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.totalBytesOut || 0), 0)
);
const totalGb = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.totalGb || 0), 0)
);
const totalCost = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.bandwidthCost || 0), 0)
);
const totalInstances = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.instanceCount || 0), 0)
);
const totalRecords = computed(() =>
  costReport.value.reduce((sum, r) => sum + (r.recordCount || 0), 0)
);

// Max clientes en un bucket (para ancho de barra)
const maxClients = computed(() =>
  Math.max(1, ...bucketReport.value.map(b => b.clientCount || 0))
);

const bucketBarWidth = (count) => {
  return Math.max(8, (count / maxClients.value) * 100);
};

const formatBytes = (bytes) => {
  if (bytes == null || bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const value = (bytes / Math.pow(k, i)).toFixed(2);
  return `${value} ${units[i]}`;
};

const loadData = async () => {
  isLoading.value = true;
  try {
    const [reportRes, distRes] = await Promise.all([
      api.get('/api/bandwidth/report', { params: { period: selectedPeriod.value } }),
      api.get('/api/bandwidth/distribution', { params: { period: selectedPeriod.value } }),
    ]);
    costReport.value = Array.isArray(reportRes.data) ? reportRes.data : [];
    bucketReport.value = Array.isArray(distRes.data) ? distRes.data : [];

    if (costReport.value.length === 0 && bucketReport.value.length === 0) {
      show({
        message: `No hay datos de ancho de banda para el periodo ${selectedPeriod.value}.`,
        severity: 'warning',
        autoHideMs: 4000,
      });
    }
  } catch (err) {
    console.error('Error cargando reportes de bandwidth:', err);
    show({
      message: 'Error al cargar los reportes de ancho de banda.',
      severity: 'error',
      autoHideMs: 4000,
    });
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadData);
</script>

<style scoped>
.bandwidth-dashboard {
  display: flex;
  justify-content: center;
  padding: 24px 16px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.bandwidth-card {
  width: 100%;
  max-width: 1200px;
}

/* Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.title-section h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  color: #1e293b;
}

.title-section p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.period-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.period-selector label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.period-input {
  padding: 7px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  color: #334155;
  background: #fff;
  cursor: pointer;
}

.period-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

/* Loading */
.loading-state {
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 32px;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #e2e8f0;
}

/* Sections */
.section-block {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1e293b;
  font-weight: 700;
}

.section-desc {
  margin: 0 0 20px 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.section-desc code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: #6366f1;
  font-family: 'Fira Code', 'Consolas', monospace;
}

.pipeline-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background-color: #eef2ff;
  color: #4f46e5;
  border: 1px solid #c7d2fe;
}

.bucket-badge {
  background-color: #fef3c7;
  color: #b45309;
  border-color: #fde68a;
}

/* Tables */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  color: #475569;
}

.data-table th {
  color: #64748b;
  font-weight: 600;
  text-align: left;
  border-bottom: 2px solid #e2e8f0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.text-right { text-align: right; }
.text-center { text-align: center; }

.mono {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
}

.highlight {
  font-weight: 700;
  color: #1e293b;
}

.user-badge {
  background: #f0f9ff;
  color: #0369a1;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
}

.cost-col {
  min-width: 160px;
}

.cost-value {
  font-weight: 700;
  color: #059669;
  font-size: 15px;
}

.data-table tfoot td {
  border-top: 2px solid #e2e8f0;
  border-bottom: none;
  padding-top: 16px;
}

.total-label {
  font-weight: 800;
  color: #1e293b;
  font-size: 15px;
}

.total-cost {
  font-weight: 800;
  color: #059669;
  font-size: 18px;
}

/* Bucket chart */
.bucket-chart {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bucket-bar-container {
  display: grid;
  grid-template-columns: 140px 1fr 180px;
  align-items: center;
  gap: 12px;
}

.bucket-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  text-align: right;
}

.bucket-bar-wrapper {
  height: 32px;
  background: #f1f5f9;
  border-radius: 6px;
  overflow: hidden;
}

.bucket-bar {
  height: 100%;
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  transition: width 0.5s ease;
  min-width: 60px;
}

.bar-text {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.bucket-color-0 { background: linear-gradient(135deg, #22c55e, #16a34a); }
.bucket-color-1 { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.bucket-color-2 { background: linear-gradient(135deg, #f59e0b, #d97706); }
.bucket-color-3 { background: linear-gradient(135deg, #ef4444, #dc2626); }
.bucket-color-4 { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.bucket-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
}

.bucket-gb {
  font-weight: 600;
  color: #334155;
  font-family: 'Fira Code', 'Consolas', monospace;
}

.bucket-cost {
  font-weight: 700;
  color: #059669;
}

/* Range badges */
.range-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.bucket-bg-0 { background: #22c55e; }
.bucket-bg-1 { background: #3b82f6; }
.bucket-bg-2 { background: #f59e0b; }
.bucket-bg-3 { background: #ef4444; }
.bucket-bg-4 { background: #8b5cf6; }

.bucket-table td,
.bucket-table th {
  padding: 12px;
}

/* Pricing info */
.pricing-info {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.pricing-info h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #334155;
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.pricing-tier {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pricing-tier.free {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.tier-range {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.tier-price {
  font-size: 14px;
  font-weight: 700;
  color: #059669;
}

.pricing-tier.free .tier-price {
  color: #16a34a;
}

/* Buttons */
.action-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.reload-btn {
  background-color: #f1f5f9;
  color: #475569;
}

.reload-btn:hover {
  background-color: #e2e8f0;
}

/* Responsive */
@media (max-width: 900px) {
  .card-header {
    flex-direction: column;
  }

  .header-actions {
    flex-direction: column;
    width: 100%;
  }

  .bucket-bar-container {
    grid-template-columns: 1fr;
  }

  .bucket-label {
    text-align: left;
  }

  .pricing-grid {
    grid-template-columns: 1fr;
  }
}
</style>
