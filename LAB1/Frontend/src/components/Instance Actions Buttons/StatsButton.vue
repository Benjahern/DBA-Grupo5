<template>
    <button @click="handleClick" class="stats-btn" :disabled="isRunning">
        <i class="pi pi-chart-bar" v-if="!isRunning"></i>
        <i class="pi pi-spin pi-spinner" v-else></i>
    </button>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAlert } from '../Alerts/useAlert.js';

const props = defineProps({
    instanceId: {
        type: [Number, String],
        required: false
    }
});

const isRunning = ref(false);
const router = useRouter();
const { show } = useAlert();

const handleClick = async () => {
    if (!props.instanceId) {
        show({
            message: 'No se pudo abrir estadisticas: falta el id de la instancia.',
            severity: 'error',
            autoHideMs: 4000
        });
        return;
    }

    isRunning.value = true;

    try {
        await router.push({ name: 'instance-stats', params: { id: String(props.instanceId) } });
    } finally {
        isRunning.value = false;
    }
};
</script>

<style scoped>
.stats-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 8px;
    background-color: #0ea5a4;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    transition: background-color 0.2s;
}

.stats-btn:hover {
    background-color: #0f766e;
}

.stats-btn:disabled {
    background-color: #99f6e4;
    cursor: not-allowed;
}
</style>