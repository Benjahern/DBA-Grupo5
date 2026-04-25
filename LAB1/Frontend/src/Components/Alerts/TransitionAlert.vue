<!--Acá se arma la caja de la notificación dinámica -->

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div 
        v-if="alerts.isVisible" 
        class="global-alert"
        :class="`alert-${alerts.severity}`"
      >
        <span>{{ alerts.message }}</span>
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
  z-index: 1400;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  min-width: 300px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  font-family: 'Roboto';
  font-weight: bold;
}
.alert-success { background-color: #42bb48; }
.alert-error { background-color: #f51717; }
.alert-info { background-color: #1facf7; }
.alert-warning { background-color: #ffbb00; }

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