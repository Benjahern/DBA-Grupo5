<template>
  <span :style="{ backgroundColor: color, width: '14px', height: '14px', display: 'inline-block', borderRadius: '50%' }"></span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'stopped'
  }
});

const color = computed(() => {
  switch (props.variant) {
    case 'running': return '#0787ff';
    case 'stopped': return '#ffaf03';
    case 'terminated': return '#ff0d00';
    default: return '#ffaf03';
  }
});
</script>