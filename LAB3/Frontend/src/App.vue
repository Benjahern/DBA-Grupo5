<script setup>
import { computed } from 'vue';
import Sidebar from './components/Structure/Sidebar.vue';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
import AlertsPanel from './components/Alerts/AlertsPanel.vue';
import router from './routes/index.js';
import api from './services/http-common.js';
import { clearSession, getUser } from './services/auth.js';

// Nombre desde el perfil guardado localmente.
const userName = computed(() => {
  const user = getUser();
  return user?.given_name
      || user?.name
      || user?.preferred_username
      || user?.email
      || 'Usuario';
});

// ID del usuario para el stream de alertas
const userId = computed(() => {
  const user = getUser();
  return user?.sub || user?.id || null;
});

const handleLogout = async () => {
  try {
    await api.post('/api/auth/logout');
  } catch (e) {
    // Si falla el logout seguimos limpiando sesión local.
  }
  clearSession();
  router.push({ name: 'login' });
};

const showSidebar = computed(() => {
  // La presencia del perfil en localStorage es la señal local de "autenticado".
  // El backend puede invalidar las cookies en cualquier momento vía /api/auth/refresh
  // o /api/auth/logout, pero el sidebar no necesita consultarlo en cada render.
  const isAuthenticated = !!getUser();
  const isLandingPage = router.currentRoute.value.name === 'landing';
  return isAuthenticated && !isLandingPage;
});
</script>

<template>
  <div class="app-layout">
    <TransitionAlert />

    <!-- Panel de alertas en tiempo real (Change Streams) -->
    <div v-if="showSidebar && userId" class="alerts-widget">
      <AlertsPanel :user-id="userId" />
    </div>

    <Sidebar
      v-if="showSidebar"
      :user-name="userName"
      @logout="handleLogout"
    />

    <main class="main-content" :class="{ 'no-sidebar': !showSidebar }">
      <router-view></router-view>
    </main>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600&display=swap');

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f0f2f5;
  color: #333;
}

.app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
}

.alerts-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 320px;
  max-height: 400px;
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.main-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.main-content.no-sidebar {
  width: 100%;
}
</style>