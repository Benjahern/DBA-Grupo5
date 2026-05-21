<template>
  <div
    class="notification-card"
    :class="{ unread: !notification.read }"
  >
    <!-- Indicador -->
    <div
      class="notification-indicator"
      :class="notification.type"
    ></div>

    <!-- Contenido -->
    <div class="notification-content">

      <div class="notification-top">

        <div>
          <h3 class="notification-title">
            {{ notification.title }}
          </h3>

          <p class="notification-message">
            {{ notification.message }}
          </p>
        </div>

        <span class="notification-time">
          {{ notification.time }}
        </span>

      </div>

      <!-- Footer -->
      <div class="notification-footer">

        <span
          v-if="!notification.read"
          class="badge"
        >
          Nueva
        </span>

        <div class="actions">

          <button
            class="mark-button"
            @click="$emit('mark-as-read', notification.id)"
            v-if="!notification.read"
          >
            Marcar como leída
          </button>

          <button
            class="delete-button"
            @click="$emit('delete', notification.id)"
          >
            Eliminar
          </button>

        </div>

      </div>

    </div>
  </div>
</template>

<script setup>

defineProps({
  notification: {
    type: Object,
    required: true
  }
})

defineEmits([
  'mark-as-read',
  'delete'
])

</script>

<style scoped>

.notification-card {
  display: flex;
  gap: 16px;
  padding: 18px;
  border-radius: 16px;
  background-color: #1e1e1e;
  border: 1px solid #e5e7eb;
  transition: 0.2s ease;
}

.notification-card:hover {
  transform: translateY(-2px);
  border-color: #f9fafb;
}

.unread {
  border-left: 4px solid #4f8cff;
  background-color: #252525;
}

.notification-indicator {
  width: 12px;
  min-width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-top: 8px;
}

.notification-indicator.overdue {
  background-color: #ff5c5c;
}

.notification-indicator.reminder {
  background-color: #4f8cff;
}

.notification-indicator.completed {
  background-color: #3ecf8e;
}

.notification-content {
  flex: 1;
}

.notification-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.notification-title {
  margin: 0;
  color: white;
  font-size: 16px;
}

.notification-message {
  margin-top: 6px;
  color: #b3b3b3;
  font-size: 14px;
}

.notification-time {
  color: #888;
  font-size: 13px;
  white-space: nowrap;
}

.notification-footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  background-color: #4f8cff;
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
}

.actions {
  display: flex;
  gap: 10px;
}

.mark-button,
.delete-button {
  border: none;
  padding: 8px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s ease;
}

.mark-button {
  background-color: #4f8cff;
  color: white;
}

.mark-button:hover {
  background-color: #3d76db;
}

.delete-button {
  background-color: #2f2f2f;
  color: #d5d5d5;
}

.delete-button:hover {
  background-color: #444;
}

</style>