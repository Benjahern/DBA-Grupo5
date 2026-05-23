<!--Acá se arma la caja de la notificación dinámica -->

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div 
        v-if="alerts.isVisible" 
        class="global-alert"
        :class="`alert-${alerts.severity}`"
      >
        <span class="icon-container">
          <svg v-if="alerts.severity === 'error'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <svg v-if="alerts.severity === 'warning'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.19 2 1.732 2z" />
          </svg>
          <svg v-if="alerts.severity === 'info'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <svg v-if="alerts.severity === 'success'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" width="24" height="24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </span>
        <span class="message-text">{{ alerts.message }}</span>
        <button class="close-btn" @click="hide">✕</button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useAlert } from './useAlert';

// Importaciones de las funciones para ver y ocultar la alerta
const { alerts, hide } = useAlert();
</script>

<style scoped>
/* Estilos básicos para la alerta*/
.global-alert {
  position: fixed;
  top: 72px;
  right: 16px;
  z-index: 3000;
  padding: 16px 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
  font-size: 16px;
  width: 450px;
  max-width: 90vw;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  font-family: 'Roboto', sans-serif;
  font-weight: 500;
}
.alert-success { background-color: #6eb05e; }
.alert-error { background-color: #d7544e; }
.alert-info { background-color: #609df0; }
.alert-warning { 
  background-color: #e59d3a; 
  color: white; 
}
.alert-warning .close-btn {
  color: white; 
}

.icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-text {
  flex-grow: 1;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  margin-left: auto;
  font-weight: bold;
}

/* Animaciones */
.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>