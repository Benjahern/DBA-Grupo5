<script setup>
import { onMounted, ref } from 'vue';
import Sidebar from './components/Structure/Sidebar.vue';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
import router from './routes';
import api from './services/http-common.js';
import { clearSession, getToken, getUser, subscribe } from './services/auth.js';

const sessionName = ref('Usuario');
const sessionRole = ref('Usuario');

const readStoredUser = () => {
  try {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch (error_) {
    return null;
  }
};

const resolveUserName = () => {
  const stored = readStoredUser();
  if (stored?.name) return stored.name;
  const tokenUser = getUser();
  return tokenUser?.given_name || tokenUser?.name || tokenUser?.preferred_username || tokenUser?.email || 'Usuario';
};

const resolveUserRole = () => {
  const stored = readStoredUser();
  return stored?.role || 'Usuario';
};

const fetchCurrentUser = async () => {
  try {
    const response = await api.get('/api/users/me');
    if (response?.data) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
  } catch (error_) {
    // ignore
  }
};

const refreshSession = async () => {
  const stored = readStoredUser();
  if (!stored && getToken()) {
    await fetchCurrentUser();
  }
  sessionName.value = resolveUserName();
  sessionRole.value = resolveUserRole();
};

const handleLogout = () => {
  console.log("Eliminando token y cerrando sesión...");
  clearSession();
  router.push({ name: 'login' });  // Salta a la vista de Login usando el router
};

onMounted(() => {
  refreshSession();
  subscribe(() => { refreshSession(); });
});
</script>

<template>
  <div class="app-layout">
    <TransitionAlert />
    
    <Sidebar 
      :user-name="sessionName"
      :user-role="sessionRole"
      :active-section="currentSection"
      @update:section="handleSectionChange"
      @logout="handleLogout"
    />

    <main class="main-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<style>
*{
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f0f2f5; /* Un fondo gris muy suave */
  color: #333;
}

.app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* Si no hay sidebar, aseguramos que el contenido use todo el espacio */
.main-content.no-sidebar {
  width: 100%;
}
</style>
