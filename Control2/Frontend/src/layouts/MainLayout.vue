<template>
  <div class="layout">
    <Sidebar class="fixed-sidebar" />

    <div class="main-content">
      <Navbar />

      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from '../components/Layout/Sidebar.vue';
import Navbar from '../components/Layout/Navbar.vue';
</script>

<style scoped>
/* Contenedor principal: Congela el tamaño total a la pantalla del dispositivo */
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden; /* Prohibido el scroll en la ventana global del navegador */
  box-sizing: border-box;
}

/* Opcional: Asegura que el Sidebar mantenga su comportamiento interno */
.fixed-sidebar {
  height: 100%;
  overflow-y: auto; /* Si el menú del sidebar es muy largo, tendrá su propio scroll interno */
}

/* Columna derecha: Aloja el Navbar y la sección de la página */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden; /* Evita que esta sección empuje el layout hacia abajo */
}

/* SOLUCIÓN: Único elemento con permiso de scroll vertical en la aplicación */
.page-content {
  padding: 2rem;
  background-color: #f4f4f4;
  flex: 1;            /* Toma todo el alto restante debajo del Navbar */
  overflow-y: auto;   /* ¡CLAVE! Si la tabla o la vista de sectores bajan, el scroll aparece AQUÍ */
  box-sizing: border-box;
}
</style>