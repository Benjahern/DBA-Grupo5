<template>
  <div class="alerts-panel">
    <!-- Header del panel -->
    <div class="alerts-header">
      <h3>Alertas en Tiempo Real</h3>
      <div class="alert-controls">
        <span class="alert-count" v-if="unreadCount > 0">
          {{ unreadCount }} nueva{{ unreadCount !== 1 ? 's' : '' }}
        </span>
        <button 
          v-if="alerts.length > 0" 
          @click="clearAlerts"
          class="btn-clear"
          title="Limpiar todas las alertas"
        >
          ✕
        </button>
      </div>
    </div>

    <!-- Lista de alertas -->
    <div class="alerts-container">
      <div v-if="alerts.length === 0" class="empty-state">
        <p>No hay alertas por el momento</p>
      </div>

      <div 
        v-for="(alert, index) in alerts" 
        :key="index"
        :class="['alert-item', alert.alertType, { unread: !alert.read }]"
      >
        <!-- Badge del tipo de alerta -->
        <div class="alert-badge" :class="alert.alertType">
          <span v-if="alert.alertType === 'BANDWIDTH_QUOTA_EXCEEDED' || alert.alertType === 'CPU_USAGE_HIGH'">⚠️</span>
          <span v-else>ℹ️</span>
        </div>

        <!-- Contenido de la alerta -->
        <div class="alert-content">
          <p class="alert-type">{{ formatAlertType(alert.alertType) }}</p>
          <p class="alert-message">{{ alert.message }}</p>
          <p class="alert-time">{{ formatTime(alert.timestamp) }}</p>
        </div>

        <!-- Acciones -->
        <div class="alert-actions">
          <button 
            v-if="!alert.read"
            @click="markAsRead(index)"
            class="btn-read"
            title="Marcar como leído"
          >
            ✓
          </button>
          <button 
            @click="removeAlert(index)"
            class="btn-close"
            title="Descartar"
          >
            ✕
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAlertsStream } from './useAlertsStream.js';

const props = defineProps({
  userId: {
    type: Number,
    required: true,
  },
});

const { alerts, markAsRead, clearAlerts, removeAlert } = useAlertsStream(props.userId);

/**
 * Cuenta las alertas no leídas
 */
const unreadCount = computed(() => {
  return alerts.value.filter(a => !a.read).length;
});

/**
 * Formatea el tipo de alerta para mostrar al usuario
 */
const formatAlertType = (type) => {
  const types = {
    BANDWIDTH_QUOTA_EXCEEDED: 'Límite de Ancho de Banda Excedido',
    CPU_USAGE_HIGH: 'Uso de CPU Muy Alto',
    CPU_THRESHOLD: 'Umbral de CPU Alcanzado',
    MEMORY_THRESHOLD: 'Umbral de Memoria Alcanzado',
  };
  return types[type] || type;
};

/**
 * Formatea la fecha/hora relativa
 */
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Ahora mismo';
  if (diffMins < 60) return `hace ${diffMins} min`;
  if (diffHours < 24) return `hace ${diffHours}h`;
  if (diffDays < 7) return `hace ${diffDays}d`;
  
  return date.toLocaleDateString('es-ES');
};
</script>

<style scoped>
.alerts-panel {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.alerts-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f9f9f9;
}

.alerts-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.alert-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.alert-count {
  background: #ff6b6b;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.btn-clear {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 18px;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-clear:hover {
  background: #f0f0f0;
}

.alerts-container {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  padding: 24px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.alert-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  transition: background 0.2s;
}

.alert-item.unread {
  background: #fffbf0;
}

.alert-item:hover {
  background: #f9f9f9;
}

.alert-badge {
  font-size: 18px;
  flex-shrink: 0;
  padding-top: 2px;
}

.alert-badge.BANDWIDTH_QUOTA_EXCEEDED,
.alert-badge.CPU_USAGE_HIGH {
  color: #ff9800;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-type {
  margin: 0 0 4px 0;
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.alert-message {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.alert-time {
  margin: 0;
  font-size: 11px;
  color: #999;
}

.alert-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.btn-read,
.btn-close {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 6px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-read:hover {
  background: #e8f5e9;
  color: #4caf50;
}

.btn-close:hover {
  background: #ffebee;
  color: #f44336;
}

/* Scrollbar personalizado */
.alerts-container::-webkit-scrollbar {
  width: 6px;
}

.alerts-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.alerts-container::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.alerts-container::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>
