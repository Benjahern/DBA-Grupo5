<script setup>
import { computed, onMounted, ref } from 'vue';
import Sidebar from './components/Structure/Sidebar.vue';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
import router from './routes/index.js';
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
  if (stored?.role) return stored.role;
  // Try to extract from JWT token's realm_access.roles
  const tokenUser = getUser();
  const roles = tokenUser?.realm_access?.roles || tokenUser?.roles || [];
  if (Array.isArray(roles) && roles.includes('admin')) return 'admin';
  return 'Usuario';
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
  router.push({ name: 'login' });
};

onMounted(() => {
  refreshSession();
  subscribe(() => { refreshSession(); });
});

const showSidebar = computed(() => {
  const isAuthenticated = !!getToken();
  const isLandingPage = router.currentRoute.value.name === 'landing';
  return isAuthenticated && !isLandingPage;
});
</script>

<template>
  <div class="app-layout">
    <TransitionAlert />

    <Sidebar
      v-if="showSidebar"
      :user-name="sessionName"
      :user-role="sessionRole"
      :active-section="currentSection"
      @update:section="handleSectionChange"
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