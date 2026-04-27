<script setup lang="ts">
import { reactive } from 'vue';
import Sidebar from './components/Structure/Sidebar.vue';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
import router from './routes';

const session = reactive({
  name: 'Admin User',
  role: 'SysAdmin',
  token: localStorage.getItem('token')
});

const handleLogout = () => {
  console.log("Eliminando token y cerrando sesión...");
  localStorage.removeItem('token'); // Borra la credencial de seguridad
  router.push({ name: 'login' });  // Salta a la vista de Login usando el router
};
</script>

<template>
  <div class="app-layout">
    <TransitionAlert />
    
    <Sidebar 
      :user-name="session.name"
      :user-role="session.role"
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
