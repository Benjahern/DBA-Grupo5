<template>
  <span
    class="badge"
    :class="variantClass"
    :title="title"
    :aria-label="ariaLabelComputed"
  ></span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'stopped'
  },
  title: {
    type: String,
    default: ''
  },
  ariaLabel: {
    type: String,
    default: ''
  }
});

const ariaLabelComputed = computed(() => {
  if (props.ariaLabel) return props.ariaLabel;
  if (props.title) return props.title;
  if (props.variant === 'running') return 'Running';
  if (props.variant === 'stopped') return 'Stopped';
  if (props.variant === 'terminated') return 'Terminated';
  return 'Estado';
});

const variantClass = computed(() => `badge--${props.variant}`);
</script>

<style scoped>
.badge {
  box-sizing: border-box;
  display: inline-block;
  flex: 0 0 auto;
  min-width: 0;
  min-height: 0;
  width: 14px;
  height: 14px;
  min-width: 14px;
  max-width: 14px;
  min-height: 14px;
  max-height: 14px;
  border-radius: 50%;
  vertical-align: middle;
  line-height: 0;
}

.badge--running {
  background-color: #0787ff;
  box-shadow: 0 0 8px rgba(40, 183, 255, 0.45);
  animation: pulse-running 1.5s infinite;
}

.badge--stopped {
  background-color: #ffaf03;
  box-shadow: 0 0 8px rgba(229, 157, 58, 0.45);
}

.badge--terminated {
  background-color: #ff0d00;
  box-shadow: 0 0 8px rgba(215, 84, 78, 0.45);
}

@keyframes pulse-running {
  0% { box-shadow: 0 0 4px rgba(96, 157, 240, 0.35); }
  50% { box-shadow: 0 0 8px rgba(96, 157, 240, 0.55); }
  100% { box-shadow: 0 0 4px rgba(96, 157, 240, 0.35); }
}
</style>
