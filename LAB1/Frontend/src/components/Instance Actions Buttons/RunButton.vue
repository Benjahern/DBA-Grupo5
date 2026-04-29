<template>
    <button @click="handleClick" class="run-btn" :disabled="isRunning">
        <i class="pi pi-caret-right" v-if="!isRunning"></i>
        <i class="pi pi-spin pi-spinner" v-else></i>
    </button>
</template>

<script setup>
import { ref } from 'vue';
import api from '../../services/http-common.js';
import { useAlert } from '../Alerts/useAlert.js';

const isRunning = ref(false);

const { show } = useAlert();

const getActionError = (err) => {
    if (err.response?.status === 401){
        show({
            message: 'No tienes los permisos necesarios para realizar esta acción.',
            severity: 'error',
            autoHideMs: 4000
        });
        return;
    }
    if (err.response?.status === 404) {
        show({
            message: 'No se encontró la instancia. Comuniquese con un administrador.',
            severity: 'error',
            autoHideMs: 4000
        })
        return;
    }
    if (err.response?.status >= 500) {
        show({
            message: 'Error del servidor. Por favor intente nuevamente más tarde.',
            severity: 'error',
            autoHideMs: 4000
        })
        return;
    }
    show({
        message: 'Ocurrió un error al ejecutar la instancia. Por favor intente nuevamente más tarde.',
        severity: 'error',
        autoHideMs: 4000
    })
};

const handleClick = async () => {
    isRunning.value = true;

    try {
        await api.post('', {});
        show({
            message: 'Instancia ejecutada exitosamente.',
            severity: 'success',
            autoHideMs: 4000
        });
    } catch (err){
        getActionError(err);
    } finally {
        isRunning.value = false;
    }
};


</script>

<style scoped>
.run-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 8px;
    background-color: #2563eb;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    transition: background-color 0.2s;
}

.run-btn:hover {
    background-color: #1d4ed8;
}

.run-btn:disabled {
    background-color: #93c5fd;
    cursor: not-allowed;
}
</style>