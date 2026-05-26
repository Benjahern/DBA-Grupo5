<template>

  <div class="notifications-view">

    <!-- Header -->
    <div class="header">

      <div>
        <h1>
          Notificaciones
        </h1>

        <p>
          Revisa la actividad reciente de tus tareas
        </p>
      </div>

      <button
        class="read-all-button"
        @click="markAllAsRead"
      >
        Marcar todas como leídas
      </button>

    </div>

    <!-- Filtros -->
    <NotificationFilters
      :search="search"
      :status="status"
      :type="type"
      @update:search="search = $event"
      @update:status="status = $event"
      @update:type="type = $event"
    />

    <!-- Lista -->
    <div
      v-if="filteredNotifications.length > 0"
      class="notifications-list"
    >

      <NotificationCard
        v-for="notification in filteredNotifications"
        :key="notification.id"
        :notification="notification"
        @mark-as-read="markAsRead"
        @delete="deleteNotification"
      />

    </div>

    <!-- Empty State -->
    <NotificationEmptyState
      v-else
    />

  </div>

</template>

<script setup>

import { computed, ref, onMounted } from 'vue'

import NotificationCard from '../components/Notifications/NotificationCard.vue'
import NotificationFilters from '../components/Notifications/NotificationFilters.vue'
import NotificationEmptyState from '../components/Notifications/NotificationsEmptyState.vue'
import { getNotifications, markAsRead as markAsReadApi, markAllAsRead as markAllAsReadApi } from '../services/notifications.js'

const search = ref('')
const status = ref('all')
const type = ref('all')

const notifications = ref([])

const filteredNotifications = computed(() => {

  return notifications.value.filter(notification => {

    const matchesSearch =
      notification.title.toLowerCase().includes(search.value.toLowerCase()) ||
      notification.message.toLowerCase().includes(search.value.toLowerCase())

    const matchesStatus =
      status.value === 'all' ||
      (status.value === 'read' && notification.read) ||
      (status.value === 'unread' && !notification.read)

    const matchesType =
      type.value === 'all' ||
      notification.type === type.value

    return (
      matchesSearch &&
      matchesStatus &&
      matchesType
    )

  })

})

async function markAsRead(id) {
  await markAsReadApi(id)
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    notification.read = true
  }
}

function deleteNotification(id) {

  notifications.value = notifications.value.filter(
    notification => notification.id !== id
  )

}

async function markAllAsRead() {
  await markAllAsReadApi()
  notifications.value.forEach(notification => {
    notification.read = true
  })
}

onMounted(async () => {
  notifications.value = await getNotifications()
})

</script>

<style scoped>

.notifications-view {
  padding: 32px;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  gap: 20px;
  flex-wrap: wrap;
}

.header h1 {
  color: black;
  margin: 0;
}

.header p {
  color: #717070;
  margin-top: 8px;
}

.read-all-button {
  border: none;
  background-color: #374151;
  color: white;
  padding: 12px 18px;
  border-radius: 12px;
  cursor: pointer;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: 28px;
}

</style>