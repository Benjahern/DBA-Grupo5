<template>
    <div class="instance-container">
        <div class="status-cell">
            <Badge v-if="badgeVariant" :variant="badgeVariant" :title="localState" />
        </div>
        <p> {{ instance.name }}</p>
        <p> {{ instance.region }}</p>
        <p> {{ instance.ip }}</p>
        <p> {{ localState }}</p>
        <p> {{ instance.cpu }}</p>
        <p> {{ instance.ram }}</p>
        <p> {{ instance.storage }}</p>

        <div class="buttons">
            <RunButton v-if="isPaused" :instance-id="instanceId" @updated="handleUpdated" />
            <PauseButton v-if="isRunning" :instance-id="instanceId" @updated="handleUpdated" />
            <DeleteButton :instance-id="instanceId" @updated="handleUpdated" />
            <StatsButton :instance-id="instanceId" />
        </div>
    </div>
</template>



<script setup>
import { computed, ref, watch } from 'vue';
import RunButton from '../Instance Actions Buttons/RunButton.vue';
import PauseButton from '../Instance Actions Buttons/PauseButton.vue';
import DeleteButton from '../Instance Actions Buttons/DeleteButton.vue';
import StatsButton from '../Instance Actions Buttons/StatsButton.vue';
import Badge from '../Badges/Badge.vue';
import { statusToBadgeVariant } from '../Badges/statusToBadgeVariant.js';

const props = defineProps({
    instance: Object
});

const emit = defineEmits(['updated']);

const localState = ref(props.instance?.state ?? '');

watch(
    () => props.instance?.state,
    (nextState) => {
        localState.value = nextState ?? '';
    }
);

const stateValue = computed(() => String(localState.value || '').toLowerCase());
const isRunning = computed(() => stateValue.value === 'running');
const isPaused = computed(() => stateValue.value === 'stopped');
const instanceId = computed(() => props.instance?.id ?? props.instance?.instance_id);
const badgeVariant = computed(() => statusToBadgeVariant(localState.value));

const handleUpdated = (nextState) => {
    localState.value = nextState;
    emit('updated');
};

</script>

<style scoped>
.instance-container {
    border: 1px solid #ccc;
    border-radius: 7px;
    padding: 4px;
    margin: 16px 0;
    display: grid;
    grid-template-columns: 40px 1fr 0.6fr 0.8fr 0.8fr 0.6fr 0.5fr 0.6fr 120px;
    gap: 8px;
    align-items: center;
}

.instance-container p {
    margin: 0;
    align-content: center;
}

.status-cell {
    display: flex;
    align-items: center;
    justify-content: center;
}

.buttons {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
}

</style>