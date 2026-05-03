<script setup>
import { computed } from 'vue';
import Sidebar from './components/Structure/Sidebar.vue';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
import router from './routes/index.js';
import { clearSession, getToken, getUser } from './services/auth.js';

// Nombre desde token
const userName = computed(() => {
  const user = getUser();
  return user?.given_name 
      || user?.name 
      || user?.preferred_username 
      || user?.email 
      || 'Usuario';
});

const handleLogout = () => {
  clearSession();
  router.push({ name: 'login' });
};

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