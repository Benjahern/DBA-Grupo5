<template>
  <header class="navbar">
    <h1>Sistema de Tareas Geoespaciales</h1>

    <div class="user-info">
      <div class="notification-container">
        <button class="bell-btn" @click="toggleNotifications" title="Notificaciones">
          🔔
          <span v-if="expiringTasks.length > 0" class="bell-badge">{{ expiringTasks.length }}</span>
        </button>

        <div v-if="showNotifications" class="notifications-dropdown">
          <div class="dropdown-header">
            <h3>Tareas por vencer</h3>
          </div>
          <div class="dropdown-body">
            <div v-if="expiringTasks.length === 0" class="no-notifications">
              No hay tareas por vencer
            </div>

            <div
              v-for="task in expiringTasks"
              :key="task.id"
              class="notification-item unread"
            >
              <span class="notification-icon">⏰</span>
              <div class="notification-content">
                <p><strong>{{ task.title }}</strong> vence hoy</p>
                <span class="notification-time">{{ task.dueDate }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <span>{{ displayUsername }}</span>

      <button class="logout-btn" @click="handleLogout">
        Cerrar sesión
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../services/http-common'
import { getUser, logout, restoreSession, subscribe } from '../../services/auth.js'
import { useAlert } from '../Alerts/useAlert.js'

const router = useRouter()
const user = ref(getUser())
const { show } = useAlert()
const showNotifications = ref(false)
const expiringTasks = ref([])
let pollingInterval = null
let unsubscribe = null

const displayUsername = computed(() => user.value?.username || 'Usuario')

const fetchExpiringTasks = async () => {
  try {
    const response = await api.get('/api/task/expiring')
    expiringTasks.value = response.data
  } catch (error) {
    console.error('Error fetching expiring tasks:', error)
  }
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

const handleLogout = async () => {
  try {
    await logout()
    show({ message: 'Sesión cerrada correctamente.', severity: 'success', autoHideMs: 3000 })
  } catch (error) {
    console.error('Error logout:', error)
    show({ message: 'No se pudo cerrar la sesión.', severity: 'error', autoHideMs: 4000 })
  } finally {
    router.push('/login')
  }
}

const closeDropdown = (e) => {
  if (!e.target.closest('.notification-container')) {
    showNotifications.value = false
  }
}

onMounted(() => {
  window.addEventListener('click', closeDropdown)
  fetchExpiringTasks()
  pollingInterval = setInterval(fetchExpiringTasks, 300000)
  restoreSession().finally(() => {
    user.value = getUser()
  })
  unsubscribe = subscribe((nextUser) => {
    user.value = nextUser
  })
})

onUnmounted(() => {
  window.removeEventListener('click', closeDropdown)
  if (pollingInterval) clearInterval(pollingInterval)
  if (unsubscribe) unsubscribe()
})
</script>

<style scoped>
.navbar {
  height: 70px;
  background-color: white;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, sans-serif;
}

.navbar h1 {
  font-size: 1.25rem;
  color: #1a1a1a;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

/* ==========================================================================
   CAMPANA Y DROPDOWN DE NOTIFICACIONES
   ========================================================================== */

.notification-container {
  position: relative;
  display: flex;
  align-items: center;
}

.bell-btn {
  background: none;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
  position: relative;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-btn:hover {
  background-color: #f0f2f5;
}

.bell-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: #ef4444;
  color: white;
  font-size: 0.7rem;
  font-weight: bold;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}

.notifications-dropdown {
  position: absolute;
  top: 45px;
  right: 0;
  width: 320px;
  background-color: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 1000; /* Prioridad por encima de los controles del mapa */
  overflow: hidden;
}

.dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid #edf2f7;
  background-color: #f8fafc;
}

.dropdown-header h3 {
  margin: 0;
  font-size: 0.95rem;
  color: #334155;
  font-weight: 600;
}

.dropdown-body {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: background-color 0.15s;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f8fafc;
}

.notification-item.unread {
  background-color: #f0f7ff;
}

.notification-item.unread:hover {
  background-color: #e0f0ff;
}

.notification-icon {
  font-size: 1.1rem;
  margin-top: 2px;
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-content p {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.35;
  color: #475569;
}

.notification-content strong {
  color: #1e293b;
}

.notification-time {
  font-size: 0.75rem;
  color: #94a3b8;
}

.no-notifications {
  padding: 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 0.85rem;
}

/* ==========================================================================
   BOTÓN CERRAR SESIÓN Y USUARIO
   ========================================================================== */

.user-info > span {
  font-size: 0.9rem;
  color: #4b5563;
  font-weight: 500;
}

.logout-btn {
  border: none;
  background-color: #3b424d;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.2s;
}

.logout-btn:hover {
  background-color: #2c323b;
}
</style>