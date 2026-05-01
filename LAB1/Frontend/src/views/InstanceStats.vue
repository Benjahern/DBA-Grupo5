<template>
  <div class="stats-page">
    <header class="stats-header">
      <div>
        <p class="eyebrow">Instancia</p>
        <h1>Estadisticas en vivo</h1>
        <p class="sub">ID: {{ instanceId }}</p>
      </div>
      <button class="back-btn" @click="goBack">Volver</button>
    </header>

    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <h2>CPU</h2>
          <span class="stat-value">{{ cpu }}%</span>
        </div>
        <div class="bar">
          <div class="bar-fill" :style="{ width: cpu + '%' }"></div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <h2>Memoria</h2>
          <span class="stat-value">{{ memory }}%</span>
        </div>
        <div class="bar">
          <div class="bar-fill alt" :style="{ width: memory + '%' }"></div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <h2>Disco</h2>
          <span class="stat-value">{{ storage }}%</span>
        </div>
        <div class="bar">
          <div class="bar-fill warning" :style="{ width: storage + '%' }"></div>
        </div>
      </div>
    </section>

    <section class="note">
      <h3>Conexion con backend</h3>
      <p>
        {{ isConnected ? 'Recibiendo estadisticas en vivo.' : 'Conectando al stream de estadisticas...' }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAlert } from '../components/Alerts/useAlert.js';
import { openInstanceStatsStream } from '../services/instance-stats.js';

const route = useRoute();
const router = useRouter();

const instanceId = route.params.id;
const cpu = ref(0);
const memory = ref(0);
const storage = ref(0);
const isConnected = ref(false);
const { show } = useAlert();

let streamController;

const clampPercent = (value) => {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return Math.max(0, Math.min(100, parsed));
};

const parsePercent = (value) => {
  if (!value) return 0;
  const normalized = String(value).replace('%', '').replace(',', '.');
  return clampPercent(parseFloat(normalized));
};

const parseBytes = (value) => {
  if (!value) return 0;
  const match = String(value).trim().match(/^([0-9]*\.?[0-9]+)\s*([a-zA-Z]+)$/);
  if (!match) return 0;
  const amount = parseFloat(match[1]);
  if (!Number.isFinite(amount)) return 0;
  const unit = match[2].toLowerCase();

  const table = {
    b: 1,
    kb: 1000,
    kib: 1024,
    mb: 1000 ** 2,
    mib: 1024 ** 2,
    gb: 1000 ** 3,
    gib: 1024 ** 3,
    tb: 1000 ** 4,
    tib: 1024 ** 4,
  };

  return amount * (table[unit] || 1);
};

const parseBlockIoPercent = (value) => {
  if (!value) return 0;
  const parts = String(value).split('/').map((part) => part.trim());
  if (parts.length < 2) return 0;
  const readBytes = parseBytes(parts[0]);
  const writeBytes = parseBytes(parts[1]);
  const total = readBytes + writeBytes;
  if (!total) return 0;
  return clampPercent((writeBytes / total) * 100);
};

const updateStatsFromLine = (line) => {
  if (!line) return;
  const trimmed = line.trim();
  if (!trimmed || trimmed.toUpperCase().startsWith('CONTAINER')) return;

  const parts = trimmed.split(/\s{2,}/);
  if (parts.length < 5) return;

  const cpuValue = parts[2];
  const memPercent = parts[4];
  const blockIo = parts[6];

  cpu.value = parsePercent(cpuValue);
  memory.value = parsePercent(memPercent);
  storage.value = parseBlockIoPercent(blockIo);
};

const startStatsStream = () => {
  if (!instanceId) {
    show({
      message: 'No se encontro el id de la instancia.',
      severity: 'error',
      autoHideMs: 4000
    });
    return;
  }

  isConnected.value = false;
  streamController = openInstanceStatsStream(instanceId, {
    onLine: (line) => {
      updateStatsFromLine(line);
      isConnected.value = true;
    },
    onConnected: () => {
      isConnected.value = true;
    },
    onError: () => {
      show({
        message: 'No se pudo conectar al stream de estadisticas.',
        severity: 'error',
        autoHideMs: 4000
      });
    },
  });
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  startStatsStream();
});

onBeforeUnmount(() => {
  if (streamController) {
    streamController.abort();
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600&display=swap');

:root {
  --paper: #f8fafc;
  --ink: #0f172a;
  --muted: #64748b;
  --accent: #0ea5a4;
  --accent-strong: #0f766e;
  --gold: #f59e0b;
  --berry: #ef4444;
}

.stats-page {
  min-height: 100vh;
  padding: 32px;
  background: radial-gradient(circle at top left, #e2f1ff 0%, #f8fafc 45%, #fff7ed 100%);
  color: var(--ink);
  font-family: 'Space Grotesk', sans-serif;
}

.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--muted);
  margin: 0 0 6px;
}

.stats-header h1 {
  font-size: 32px;
  margin: 0 0 6px;
}

.sub {
  color: var(--muted);
  margin: 0;
}

.back-btn {
  border: none;
  padding: 10px 18px;
  border-radius: 999px;
  background: var(--ink);
  color: white;
  cursor: pointer;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  margin-bottom: 28px;
}

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.stat-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-header h2 {
  font-size: 18px;
  margin: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
}

.bar {
  height: 12px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(120deg, var(--accent), var(--accent-strong));
  transition: width 0.4s ease;
}

.bar-fill.alt {
  background: linear-gradient(120deg, #60a5fa, #2563eb);
}

.bar-fill.warning {
  background: linear-gradient(120deg, var(--gold), var(--berry));
}

.note {
  background: white;
  padding: 18px 20px;
  border-radius: 14px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  color: var(--muted);
}

.note h3 {
  margin: 0 0 8px;
  color: var(--ink);
}

@media (max-width: 700px) {
  .stats-page {
    padding: 20px;
  }

  .stats-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
