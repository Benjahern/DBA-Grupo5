<template>
  <aside class="sidebar">

    <h2 class="logo">
      GeoTasks
    </h2>

    <nav class="menu">

      <router-link to="/dashboard">
        Dashboard
      </router-link>

      <router-link to="/tasks">
        Tareas
      </router-link>

      <router-link to="/notifications">
        Notificaciones
      </router-link>

      <router-link v-if="isAdmin" to="/sectors">
        Sectores
      </router-link>

    </nav>

  </aside>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { getUser, restoreSession, subscribe } from '../../services/auth.js';

const user = ref(getUser());
let unsubscribe = null;

const isAdmin = computed(() => user.value?.role === 'ADMIN');

onMounted(async () => {
  await restoreSession();
  user.value = getUser();
  unsubscribe = subscribe((nextUser) => {
    user.value = nextUser;
  });
});

onUnmounted(() => {
  if (unsubscribe) {
    unsubscribe();
  }
});
</script>

<style scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  background-color: #3b424d;
  color: white;
  padding: 1.5rem;
  box-sizing: border-box;
}

.logo {
  margin-bottom: 2rem;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.menu a {
  color: white;
  text-decoration: none;
  font-weight: 500;
}

.menu a.router-link-active {
  color: #42b983;
}
</style>