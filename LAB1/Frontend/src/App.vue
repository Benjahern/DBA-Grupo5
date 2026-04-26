<script setup lang="ts">
import TransitionAlert from './Components/Alerts/TransitionAlert.vue';
</script>

<template>
  <TransitionAlert />

  <router-view></router-view>
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
</style>
