<script setup>
import './router';
import TransitionAlert from './components/Alerts/TransitionAlert.vue';
</script>

<template>
  <TransitionAlert />
  <router-view />
</template>
