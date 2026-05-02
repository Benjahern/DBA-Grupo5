<template>
    <button @click="handleClick" class="create-btn">
        + Crear Instancias
    </button>

    <ModalNewIntance v-if="isOpen" @close="isOpen = false" />
</template>

<script setup>
import { ref } from 'vue';
import ModalNewIntance from '../Modal New Instance/ModalNewIntance.vue';

const isOpen = ref(false);

const handleClick = () => {
    isOpen.value = true;
};
</script>

<style scoped>
.create-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 8px;
    background-color: #0099ff;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    transition: background-color 0.2s;
}

.create-btn:hover {
    background-color: #0048ff;
}

.create-btn:disabled {
    background-color: #1c9bf0da;
    cursor: not-allowed;
}
</style>