<template>
    <button 
        @click="handleClick" 
        class="resilience-btn" 
        :class="{ 'icon-only': iconOnly }"
        :title="iconOnly ? 'Resiliencia Geográfica' : ''"
    >
        <svg 
            xmlns="http://www.w3.org/2000/svg" 
            width="18" 
            height="18" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor" 
            stroke-width="2" 
            stroke-linecap="round" 
            stroke-linejoin="round"
            class="globe-icon"
        >
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="2" y1="12" x2="22" y2="12"></line>
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
        </svg>
        
        <span v-if="!iconOnly">Resiliencia Geográfica</span>
    </button>

    <ModalResilience 
        v-if="isOpen" 
        :instance-id="instanceId"
        @close="isOpen = false" 
    />
</template>

<script setup>
import { ref } from 'vue';
import ModalResilience from '../Instance Container/ModalResilience.vue';

const props = defineProps({
    instanceId: {
        type: [String, Number],
        default: '' 
    },
    iconOnly: {
        type: Boolean,
        default: false 
    }
});

const isOpen = ref(false);

const handleClick = () => {
    isOpen.value = true;
};
</script>

<style scoped>
.resilience-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 12px;
    background-color: #10b981; 
    color: white; 
    border: none;
    border-radius: 4px; 
    cursor: pointer;
    font-size: 15px;
    font-weight: bold;
    transition: background-color 0.2s, transform 0.1s;
}

.resilience-btn:hover {
    background-color: #059669;
}

.resilience-btn:disabled {
    background-color: #6ee7b7;
    cursor: not-allowed;
}

.globe-icon {
    color: currentColor;
}

.resilience-btn.icon-only {
    width: 32px;
    height: 32px;
    padding: 0;
    gap: 0;
}

.resilience-btn.icon-only:hover {
    transform: scale(1.05);
}

.resilience-btn.icon-only:active {
    transform: scale(0.95);
}
</style>